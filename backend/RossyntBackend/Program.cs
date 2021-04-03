#if NET5_0
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
#elif NETCOREAPP3_1
using Microsoft.Extensions.Hosting;
#elif NETCOREAPP2_1
using Microsoft.AspNetCore;
#endif
using System.Threading.Tasks;
using Microsoft.AspNetCore.Hosting;

namespace RossyntBackend {
    // ReSharper disable once ClassNeverInstantiated.Global
    public class Program {
        public static async Task Main(string[] args) {
#if NETCOREAPP3_1 || NET5_0
            await CreateHostBuilder(args).Build().RunAsync();
#elif NETCOREAPP2_1
            await CreateWebHostBuilder(args).Build().RunAsync();
#endif
        }

#if NET5_0
        // ReSharper disable once MemberCanBePrivate.Global
        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureLogging(builder => builder.AddSimpleConsole(options => options.SingleLine = true))
                .ConfigureWebHostDefaults(webBuilder => webBuilder.UseStartup<Startup>());
#elif NETCOREAPP3_1
        // ReSharper disable once MemberCanBePrivate.Global
        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder => webBuilder.UseStartup<Startup>());
#elif NETCOREAPP2_1
        // ReSharper disable once MemberCanBePrivate.Global
        public static IWebHostBuilder CreateWebHostBuilder(string[] args) =>
            WebHost.CreateDefaultBuilder(args)
                .UseStartup<Startup>();
#endif
    }
}
