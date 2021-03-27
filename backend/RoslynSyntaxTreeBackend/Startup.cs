using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using RoslynSyntaxTreeBackend.Repositories;

namespace RoslynSyntaxTreeBackend {
    public class Startup {
        // ReSharper disable once UnusedParameter.Local
        public Startup(IConfiguration configuration) {
        }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services) {
            services.AddSingleton<IProjectRepository, ProjectRepository>();
#if NET5_0 || NETCOREAPP3_1
            services.AddControllers();
#else
            services.AddMvc();
#endif
        }

#if NET5_0 || NETCOREAPP3_1
        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env) {
            app.UseRouting();
            app.UseEndpoints(endpoints => endpoints.MapControllers());
        }
#else
        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env) {
            app.UseMvc(routes => routes.MapRoute("default", "{controller=Home}/{action=Index}/{id?}"));
        }
#endif
    }
}
