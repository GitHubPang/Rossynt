using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RossyntBackend.Controllers {
    public sealed class SetActiveFileRequest {
        // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
        [Required] [NotNull] public string FilePath { get; set; } = "";
        // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
    }
}
