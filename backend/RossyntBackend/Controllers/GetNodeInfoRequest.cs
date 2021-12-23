using System.ComponentModel.DataAnnotations;

#nullable enable

namespace RossyntBackend.Controllers {
    // ReSharper disable AutoPropertyCanBeMadeGetOnly.Global
    public sealed class GetNodeInfoRequest {
        [Required] public string NodeId { get; set; } = "";
    }
    // ReSharper restore AutoPropertyCanBeMadeGetOnly.Global
}
