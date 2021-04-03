using JetBrains.Annotations;
#if NET5_0 || NETCOREAPP3_1
using Microsoft.Extensions.Hosting;
#elif NETCOREAPP2_1
using Microsoft.AspNetCore.Hosting;
#endif
using Microsoft.Extensions.Logging;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace RossyntBackend.ApplicationLifetime {
    public sealed class ApplicationLifetimeService : IApplicationLifetimeService, IDisposable {
        private static readonly TimeSpan CountdownInterval = TimeSpan.FromSeconds(60);

        // ******************************************************************************** //

#if NET5_0 || NETCOREAPP3_1
        [NotNull] private readonly IHostApplicationLifetime _applicationLifetime;
#else
        [NotNull] private readonly IApplicationLifetime _applicationLifetime;
#endif

        [NotNull] private readonly object _lock = new object();
        [NotNull] private readonly ILogger<ApplicationLifetimeService> _logger;
        private bool _isCountdownStarted;
        private bool _isRestartCountdown;
        [NotNull] private CancellationTokenSource _cancellationTokenSource = new CancellationTokenSource();

        // ******************************************************************************** //

#if NET5_0 || NETCOREAPP3_1
        public ApplicationLifetimeService([NotNull] IHostApplicationLifetime applicationLifetime, [NotNull] ILogger<ApplicationLifetimeService> logger) {
            _applicationLifetime = applicationLifetime ?? throw new ArgumentNullException(nameof(applicationLifetime));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }
#elif NETCOREAPP2_1
        public ApplicationLifetimeService([NotNull] IApplicationLifetime applicationLifetime, [NotNull] ILogger<ApplicationLifetimeService> logger) {
            _applicationLifetime = applicationLifetime ?? throw new ArgumentNullException(nameof(applicationLifetime));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }
#endif
        public void Dispose() {
            _cancellationTokenSource.Dispose();
        }

        public void StartCountdown() {
            lock (_lock) {
                if (_isCountdownStarted) {
                    throw new InvalidOperationException("Countdown already started.");
                }

                _isCountdownStarted = true;
            }

            Task.Run(async () => {
                while (true) {
                    // Wait until timeout or cancelled.
                    try {
                        await Task.Delay(CountdownInterval, _cancellationTokenSource.Token);
                    }
                    catch (OperationCanceledException) {
                        // Nothing to do.
                    }

                    lock (_lock) {
                        // Replace cancellation token source.
                        _cancellationTokenSource.Dispose();
                        _cancellationTokenSource = new CancellationTokenSource();

                        // Restart countdown if needed.
                        if (_isRestartCountdown) {
                            _isRestartCountdown = false;
                            continue;
                        }
                    }

                    // Quit application.
                    _logger.LogInformation("Quitting application...");
                    _applicationLifetime.StopApplication();
                    break;
                }
            });
        }

        public void RestartCountdown() {
            lock (_lock) {
                _isRestartCountdown = true;
                _cancellationTokenSource.Cancel();
            }
        }

        public void QuitApplication() {
            _logger.LogInformation("Quit application requested.");
            lock (_lock) {
                _cancellationTokenSource.Cancel();
            }
        }
    }
}
