using System.ComponentModel.DataAnnotations;

namespace RossyntBackend.Controllers {
    public sealed class FindNodeRequest {
        // ReSharper disable UnusedAutoPropertyAccessor.Global
        [Required] public int Start { get; set; }
        [Required] public int End { get; set; }
        // ReSharper restore UnusedAutoPropertyAccessor.Global
    }
}
