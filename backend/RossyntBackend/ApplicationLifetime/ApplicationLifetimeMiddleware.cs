using System;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Http;

namespace RossyntBackend.ApplicationLifetime;

[UsedImplicitly]
public sealed class ApplicationLifetimeMiddleware {
    private readonly RequestDelegate _next;
    private readonly IApplicationLifetimeService _applicationLifetimeService;

    // ******************************************************************************** //

    public ApplicationLifetimeMiddleware(RequestDelegate next, IApplicationLifetimeService applicationLifetimeService) {
        _next = next ?? throw new ArgumentNullException(nameof(next));
        _applicationLifetimeService = applicationLifetimeService ?? throw new ArgumentNullException(nameof(applicationLifetimeService));
    }

    [UsedImplicitly]
    public async Task InvokeAsync(HttpContext context) {
        if (context == null) throw new ArgumentNullException(nameof(context));

        // Call the next delegate/middleware in the pipeline
        await _next(context);

        // Restart application lifetime countdown.
        _applicationLifetimeService.RestartCountdown();
    }
}
