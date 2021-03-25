using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc;
using Microsoft.CodeAnalysis.CSharp;
using RoslynSyntaxTreeBackend.Models;
using RoslynSyntaxTreeBackend.Repositories;

namespace RoslynSyntaxTreeBackend.Controllers {
    [ApiController]
    [Route("[controller]")]
    public class SyntaxTreeController : ControllerBase {
        [NotNull] private readonly IProjectRepository _projectRepository;

        // ******************************************************************************** //

        public SyntaxTreeController([NotNull] IProjectRepository projectRepository) {
            _projectRepository = projectRepository ?? throw new ArgumentNullException(nameof(projectRepository));
        }

        [HttpPost(nameof(SetActiveFile))]
        [NotNull, ItemNotNull]
        public async Task<IReadOnlyDictionary<string, object>> SetActiveFile([NotNull] [FromForm] SetActiveFileRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            // Compile tree.
            var tree = await Tree.CompileFile(request.FilePath, HttpContext.RequestAborted);

            // Save in repository.
            var projectRecord = new ProjectRecord(request.ProjectId, tree);
            _projectRepository.SetProjectRecord(projectRecord);

            return ConvertTreeToObject(tree.RootTreeNode);
        }

        [Pure]
        [NotNull]
        private static IReadOnlyDictionary<string, object> ConvertTreeToObject([NotNull] TreeNode treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            var childNodes = treeNode.ChildTreeNodes.Select(ConvertTreeToObject).ToImmutableArray();

            var result = new Dictionary<string, object> {
                ["Id"] = treeNode.NodeId,
                ["Kind"] = treeNode.SyntaxNodeOrToken.Kind().ToString()
            };
            if (childNodes.Length > 0) {
                result["Child"] = childNodes;
            }

            return result;
        }
    }
}
