using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc;
using RossyntBackend.ApplicationLifetime;
using RossyntBackend.Models;
using RossyntBackend.Repositories;
using RossyntBackend.Utils;

namespace RossyntBackend.Controllers {
    [ApiController]
    [Route("[controller]")]
    public class SyntaxTreeController : ControllerBase {
        private const int ShortStringMaxLength = 32;

        // ******************************************************************************** //

        [NotNull] private readonly IProjectRepository _projectRepository;
        [NotNull] private readonly IApplicationLifetimeService _applicationLifetimeService;

        // ******************************************************************************** //

        public SyntaxTreeController([NotNull] IProjectRepository projectRepository, [NotNull] IApplicationLifetimeService applicationLifetimeService) {
            _projectRepository = projectRepository ?? throw new ArgumentNullException(nameof(projectRepository));
            _applicationLifetimeService = applicationLifetimeService ?? throw new ArgumentNullException(nameof(applicationLifetimeService));
        }

        [HttpPost(nameof(Ping))]
        public void Ping() {
            // Restart application lifetime countdown.
            _applicationLifetimeService.RestartCountdown();
        }

        [HttpPost(nameof(CompileFile))]
        [NotNull, ItemNotNull]
        public async Task<IReadOnlyDictionary<string, object>> CompileFile([NotNull] [FromForm] CompileFileRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Restart application lifetime countdown.
            _applicationLifetimeService.RestartCountdown();

            // Compile tree.
            var tree = await Tree.CompileFile(request.FileText, request.FilePath, HttpContext.RequestAborted);

            // Save in repository.
            _projectRepository.SetTree(tree);

            // Render tree and return as response.
            return RenderTree(tree.RootTreeNode);
        }

        [HttpPost(nameof(ResetActiveFile))]
        public void ResetActiveFile() {
            // Restart application lifetime countdown.
            _applicationLifetimeService.RestartCountdown();

            // Clear in repository.
            _projectRepository.RemoveTree();
        }

        [HttpPost(nameof(GetNodeInfo))]
        [NotNull]
        public IReadOnlyDictionary<string, string> GetNodeInfo([NotNull] [FromForm] GetNodeInfoRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Restart application lifetime countdown.
            _applicationLifetimeService.RestartCountdown();

            // Get tree.
            var tree = _projectRepository.GetTree();
            if (tree == null) {
                throw new InvalidOperationException("No tree in repository.");
            }

            // Get tree node.
            if (!tree.TreeNodes.TryGetValue(request.NodeId, out var treeNode)) {
                throw new InvalidOperationException($"Tree node not found. NodeId = {request.NodeId}");
            }

            // Prepare response.
            return treeNode.RawProperties();
        }

        [Pure]
        [NotNull]
        private static IReadOnlyDictionary<string, object> RenderTree([NotNull] TreeNode treeNode) {
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
                result["Span"] = $"{textSpan.Start},{textSpan.Length}";
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
        [NotNull]
        private static string ShortString([NotNull] TreeNode treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            var rawString = treeNode.RawString();
            return rawString.Length > ShortStringMaxLength ? rawString.SurrogateSafeLeft(ShortStringMaxLength) + "…" : rawString;
        }
    }
}
