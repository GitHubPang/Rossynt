using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RoslynSyntaxTreeBackend.Models {
    public sealed class Tree {
        [NotNull] public TreeNode RootTreeNode { get; }

        // ******************************************************************************** //

        private Tree([NotNull] SyntaxNode root) {
            if (root == null) throw new ArgumentNullException(nameof(root));

            var rootTreeNode = new TreeNodeSyntaxOrToken(root, null);
            ProcessTreeNode(rootTreeNode);
            RootTreeNode = rootTreeNode;
        }

        [NotNull]
        public static async Task<Tree> CompileFile([NotNull] string filePath, CancellationToken cancellationToken) {
            var fileContent = await File.ReadAllTextAsync(filePath, cancellationToken);
            var syntaxTree = CSharpSyntaxTree.ParseText(fileContent, path: filePath, cancellationToken: cancellationToken);
            var root = await syntaxTree.GetRootAsync(cancellationToken);
            return new Tree(root);
        }

        private static void ProcessTreeNode([NotNull] TreeNodeSyntaxOrToken treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            // Process leading trivia.
            foreach (var trivia in treeNode.SyntaxNodeOrToken.GetLeadingTrivia()) {
                _ = new TreeNodeTrivia(true, trivia, treeNode);
            }

            // Process each child.
            foreach (var child in treeNode.SyntaxNodeOrToken.ChildNodesAndTokens()) {
                ProcessTreeNode(new TreeNodeSyntaxOrToken(child, treeNode));
            }

            // Process trailing trivia.
            foreach (var trivia in treeNode.SyntaxNodeOrToken.GetTrailingTrivia()) {
                _ = new TreeNodeTrivia(false, trivia, treeNode);
            }
        }
    }
}
