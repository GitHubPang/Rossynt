using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class CompileFileRequest {
        [Required(AllowEmptyStrings = true), DisplayFormat(ConvertEmptyStringToNull = false)] [NotNull] public string FileText { get; set; } = "";
        [Required] [NotNull] public string FilePath { get; set; } = "";
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
