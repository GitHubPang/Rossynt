using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class CompileFileRequest {
        [Required] [NotNull] public string FileText { get; set; } = "";
        [Required] [NotNull] public string FilePath { get; set; } = "";
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
