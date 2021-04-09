using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RossyntBackend.Controllers {
    public sealed class GetNodeInfoRequest {
        // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
        [Required] [NotNull] public string NodeId { get; set; } = "";
        // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
    }
}
