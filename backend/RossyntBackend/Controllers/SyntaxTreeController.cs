using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Net.Mime;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc;
using Microsoft.CodeAnalysis.Text;
using Newtonsoft.Json;
using RossyntBackend.Models;
using RossyntBackend.Repositories;
using RossyntBackend.Utils;

namespace RossyntBackend.Controllers {
    [ApiController]
    [Route("[controller]")]
    public class SyntaxTreeController : ControllerBase {
        private const int ShortStringMaxLength = 32;

        // ******************************************************************************** //

        private readonly IProjectRepository _projectRepository;

        // ******************************************************************************** //

        public SyntaxTreeController(IProjectRepository projectRepository) {
            _projectRepository = projectRepository ?? throw new ArgumentNullException(nameof(projectRepository));
        }

        [HttpPost(nameof(Ping))]
        public void Ping() {
            // Nothing to do.
        }

        [HttpPost(nameof(CompileFile))]
        public async Task<IActionResult> CompileFile([FromForm] CompileFileRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Compile tree.
            var tree = await Tree.CompileFile(request.FileText, request.FilePath, request.CSharpVersion, HttpContext.RequestAborted);

            // Save in repository.
            _projectRepository.SetTree(tree);

            // Render tree and return as response.
            //
            // Serialize to JSON manually because JSON serialization in ASP.NET Core 3.1+ has a max depth of 32.
            // https://docs.microsoft.com/en-us/dotnet/standard/serialization/system-text-json-migrate-from-newtonsoft-how-to?pivots=dotnet-5-0#maximum-depth
            //
            // Use Newtonsoft.Json instead of System.Text.Json because
            // the former has no max depth
            // while the latter requires .NET Core 3.0+.
            //
            var renderedTree = RenderTree(tree.RootTreeNode);
            var response = JsonConvert.SerializeObject(renderedTree);
            return Content(response, MediaTypeNames.Application.Json);
        }

        [HttpPost(nameof(ResetActiveFile))]
        public void ResetActiveFile() {
            // Clear in repository.
            _projectRepository.RemoveTree();
        }

        [HttpPost(nameof(GetNodeInfo))]
        public IReadOnlyDictionary<string, string> GetNodeInfo([FromForm] GetNodeInfoRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Get tree.
            var tree = _projectRepository.GetTree() ?? throw new InvalidOperationException("No tree in repository.");

            // Get tree node.
            if (!tree.TreeNodes.TryGetValue(request.NodeId, out var treeNode)) {
                throw new InvalidOperationException($"Tree node not found. NodeId = {request.NodeId}");
            }

            // Prepare response.
            return treeNode.RawProperties();
        }

        [HttpPost(nameof(FindNode))]
        public IReadOnlyDictionary<string, string> FindNode([FromForm] FindNodeRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Get tree.
            var tree = _projectRepository.GetTree() ?? throw new InvalidOperationException("No tree in repository.");

            // Find tree node.
            var treeNode = tree.FindTreeNode(TextSpan.FromBounds(request.Start, request.End));

            // Prepare response.
            var result = new Dictionary<string, string>();
            if (treeNode != null) {
                result["nodeId"] = treeNode.NodeId;
            }

            return result;
        }

        [Pure]
        private static IReadOnlyDictionary<string, object> RenderTree(TreeNode treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            var childNodes = treeNode.ChildTreeNodes.Select(RenderTree).ToImmutableArray();

            var result = new Dictionary<string, object> {
                ["Id"] = treeNode.NodeId,
                ["Cat"] = treeNode.TreeNodeCategory().ToString(),
                ["Type"] = treeNode.RawType().Name,
                ["Kind"] = treeNode.SyntaxKind().ToString(),
            };

            var shortString = ShortString(treeNode);
            if (shortString.Length > 0) {
                result["Str"] = shortString;
            }

            var textSpan = treeNode.Span();
            if (textSpan.Length > 0) {
                result["Span"] = textSpan.Length == 1 ? $"{textSpan.Start}" : $"{textSpan.Start},{textSpan.Length}";
            }

            if (treeNode.IsMissing()) {
                result["IsMissing"] = 1;
            }

            if (childNodes.Length > 0) {
                result["Child"] = childNodes;
            }

            return result;
        }

        [Pure]
        private static string ShortString(TreeNode treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            var rawString = treeNode.RawString();
            return rawString.Length > ShortStringMaxLength ? rawString.SurrogateSafeLeft(ShortStringMaxLength) + "…" : rawString;
        }
    }
}
