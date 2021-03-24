using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RoslynSyntaxTreeBackend.Controllers {
    public sealed class SetActiveFileRequest {
        [Required] [NotNull] public string ProjectId { get; set; } = "";
        [Required] [NotNull] public string FilePath { get; set; } = "";
    }
}
