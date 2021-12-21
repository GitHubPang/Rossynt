using System.ComponentModel.DataAnnotations;

#nullable enable

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class CompileFileRequest {
        [Required(AllowEmptyStrings = true), DisplayFormat(ConvertEmptyStringToNull = false)] public string FileText { get; set; } = "";
        [Required] public string FilePath { get; set; } = "";
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
