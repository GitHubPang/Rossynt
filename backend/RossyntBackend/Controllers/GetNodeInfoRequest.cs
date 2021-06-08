using System.ComponentModel.DataAnnotations;
using JetBrains.Annotations;

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class GetNodeInfoRequest {
        [Required] [NotNull] public string NodeId { get; set; } = "";
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
