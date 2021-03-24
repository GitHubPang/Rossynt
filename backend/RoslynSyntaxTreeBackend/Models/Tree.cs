using System;
using System.Collections.Immutable;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RoslynSyntaxTreeBackend.Models {
    public sealed class Tree {
        [NotNull] private readonly SyntaxTree _syntaxTree;
        [NotNull] private readonly SyntaxNode _root;
        [NotNull] private readonly IImmutableList<TreeNode> _treeNodes;

        // ******************************************************************************** //

        private Tree([NotNull] SyntaxTree syntaxTree, [NotNull] SyntaxNode root) {
            _syntaxTree = syntaxTree ?? throw new ArgumentNullException(nameof(syntaxTree));
            _root = root ?? throw new ArgumentNullException(nameof(root));
            _treeNodes = _root.DescendantNodesAndTokensAndSelf().Select(syntaxNodeOrToken => new TreeNode(syntaxNodeOrToken)).ToImmutableArray();
        }

        [NotNull]
        public static async Task<Tree> CompileFile([NotNull] string filePath, CancellationToken cancellationToken) {
            var fileContent = await File.ReadAllTextAsync(filePath, cancellationToken);
            var syntaxTree = CSharpSyntaxTree.ParseText(fileContent, path: filePath, cancellationToken: cancellationToken);
            var root = await syntaxTree.GetRootAsync(cancellationToken);
            return new Tree(syntaxTree, root);
        }
    }
}
