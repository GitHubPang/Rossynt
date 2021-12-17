using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RossyntBackend.ApplicationLifetime;
using RossyntBackend.Repositories;

#nullable enable

namespace RossyntBackend {
    public class Startup {
        // ReSharper disable once UnusedParameter.Local
        public Startup(IConfiguration configuration) {
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services) {
            services.AddSingleton<IProjectRepository, ProjectRepository>();
            services.AddSingleton<IApplicationLifetimeService, ApplicationLifetimeService>();
            services.AddControllers();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app) {
            app.UseMiddleware<ApplicationLifetimeMiddleware>();
            app.UseRouting();
            app.UseEndpoints(endpoints => endpoints.MapControllers());

            var applicationLifetimeService = app.ApplicationServices.GetRequiredService<IApplicationLifetimeService>();
            applicationLifetimeService.StartCountdown();
        }
    }
}
