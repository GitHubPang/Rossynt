using System.Threading.Tasks;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;

namespace RoslynSyntaxTreeBackend {
    // ReSharper disable once ClassNeverInstantiated.Global
    public class Program {
        public static async Task Main(string[] args) {
            await CreateWebHostBuilder(args).Build().RunAsync();
        }

        // ReSharper disable once MemberCanBePrivate.Global
        public static IWebHostBuilder CreateWebHostBuilder(string[] args) =>
            WebHost.CreateDefaultBuilder(args)
                .UseStartup<Startup>();
    }
}
