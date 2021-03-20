using System;
using System.Linq;
using System.Threading.Tasks;

namespace RoslynSyntaxTreeBackend {
    internal static class Program {
        private static async Task Main(string[] args) {
            Console.WriteLine(string.Join(" ", args.Reverse()));
            await Console.Out.FlushAsync();
        }
    }
}
