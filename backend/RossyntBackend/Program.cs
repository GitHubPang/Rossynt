#if NET5_0 || NET6_0
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
#elif NETCOREAPP3_1
using Microsoft.Extensions.Hosting;
#endif
using System.Threading.Tasks;
using Microsoft.AspNetCore.Hosting;

#nullable enable

namespace RossyntBackend {
    // ReSharper disable once ClassNeverInstantiated.Global
    public class Program {
        public static async Task Main(string[] args) {
            await CreateHostBuilder(args).Build().RunAsync();
        }

        // ReSharper disable once MemberCanBePrivate.Global
        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
#if NET5_0 || NET6_0
                .ConfigureLogging(builder => builder.AddSimpleConsole(options => options.SingleLine = true))
#elif NETCOREAPP3_1
#endif
                .ConfigureWebHostDefaults(webBuilder => webBuilder.UseStartup<Startup>());
    }
}
