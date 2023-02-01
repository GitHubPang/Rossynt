using System.ComponentModel.DataAnnotations;
using Microsoft.CodeAnalysis.CSharp;

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class CompileFileRequest {
        [Required(AllowEmptyStrings = true), DisplayFormat(ConvertEmptyStringToNull = false)] public string FileText { get; set; } = "";
        [Required] public string FilePath { get; set; } = "";
        public LanguageVersion CSharpVersion { get; set; } = LanguageVersion.Default;
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
