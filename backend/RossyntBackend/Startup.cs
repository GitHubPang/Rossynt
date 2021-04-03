using System;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RossyntBackend.ApplicationLifetime;
using RossyntBackend.Repositories;

namespace RossyntBackend {
    public class Startup {
        // ReSharper disable once UnusedParameter.Local
        public Startup(IConfiguration configuration) {
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services) {
            services.AddSingleton<IProjectRepository, ProjectRepository>();
            services.AddSingleton<IApplicationLifetimeService, ApplicationLifetimeService>();
#if NET5_0 || NETCOREAPP3_1
            services.AddControllers();
#elif NETCOREAPP2_1
            services.AddMvc();
#endif
        }

#if NET5_0 || NETCOREAPP3_1
        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env) {
            app.UseRouting();
            app.UseEndpoints(endpoints => endpoints.MapControllers());
            Configure(app);
        }
#elif NETCOREAPP2_1
        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env) {
            app.UseMvc(routes => routes.MapRoute("default", "{controller=Home}/{action=Index}/{id?}"));
            Configure(app);
        }
#endif

        private void Configure([NotNull] IApplicationBuilder app) {
            if (app == null) throw new ArgumentNullException(nameof(app));

            var applicationLifetimeService = app.ApplicationServices.GetRequiredService<IApplicationLifetimeService>();
            applicationLifetimeService.StartCountdown();
        }
    }
}
