using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

#nullable enable

namespace RossyntBackend.ApplicationLifetime {
    public sealed class ApplicationLifetimeService : IApplicationLifetimeService, IDisposable {
        private static readonly TimeSpan CountdownInterval = TimeSpan.FromSeconds(60);

        // ******************************************************************************** //

        private readonly IHostApplicationLifetime _applicationLifetime;

        private readonly object _lock = new object();
        private readonly ILogger<ApplicationLifetimeService> _logger;
        private bool _isCountdownStarted;
        private bool _isRestartCountdown;
        private CancellationTokenSource _cancellationTokenSource = new CancellationTokenSource();

        // ******************************************************************************** //

        public ApplicationLifetimeService(IHostApplicationLifetime applicationLifetime, ILogger<ApplicationLifetimeService> logger) {
            _applicationLifetime = applicationLifetime ?? throw new ArgumentNullException(nameof(applicationLifetime));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

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
