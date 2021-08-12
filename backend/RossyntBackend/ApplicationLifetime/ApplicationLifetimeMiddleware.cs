using System;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Http;

namespace RossyntBackend.ApplicationLifetime {
    [UsedImplicitly]
    public sealed class ApplicationLifetimeMiddleware {
        [NotNull] private readonly RequestDelegate _next;
        [NotNull] private readonly IApplicationLifetimeService _applicationLifetimeService;

        // ******************************************************************************** //

        public ApplicationLifetimeMiddleware([NotNull] RequestDelegate next, [NotNull] IApplicationLifetimeService applicationLifetimeService) {
            _next = next ?? throw new ArgumentNullException(nameof(next));
            _applicationLifetimeService = applicationLifetimeService ?? throw new ArgumentNullException(nameof(applicationLifetimeService));
        }

        [UsedImplicitly]
        public async Task InvokeAsync([NotNull] HttpContext context) {
            if (context == null) throw new ArgumentNullException(nameof(context));

            // Call the next delegate/middleware in the pipeline
            await _next(context);

            // Restart application lifetime countdown.
            _applicationLifetimeService.RestartCountdown();
        }
    }
}
