using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RoslynSyntaxTreeBackend.Models {
    public sealed class Tree {
        [NotNull] private readonly SyntaxTree _syntaxTree;
        [NotNull] private readonly SyntaxNode _root;
        [NotNull] private readonly IList<TreeNode> _treeNodes = new List<TreeNode>();

        // ******************************************************************************** //

        private Tree([NotNull] SyntaxTree syntaxTree, [NotNull] SyntaxNode root) {
            _syntaxTree = syntaxTree ?? throw new ArgumentNullException(nameof(syntaxTree));
            _root = root ?? throw new ArgumentNullException(nameof(root));
            ProcessNode(new TreeNode(_root, null));
        }

        [NotNull]
        public static async Task<Tree> CompileFile([NotNull] string filePath, CancellationToken cancellationToken) {
            var fileContent = await File.ReadAllTextAsync(filePath, cancellationToken);
            var syntaxTree = CSharpSyntaxTree.ParseText(fileContent, path: filePath, cancellationToken: cancellationToken);
            var root = await syntaxTree.GetRootAsync(cancellationToken);
            return new Tree(syntaxTree, root);
        }

        private void ProcessNode([NotNull] TreeNode treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            // Save the tree node.
            _treeNodes.Add(treeNode);

            // Process each child.
            foreach (var child in treeNode.SyntaxNodeOrToken.ChildNodesAndTokens()) {
                ProcessNode(new TreeNode(child, treeNode));
            }
        }
    }
}
