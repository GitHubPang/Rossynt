using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RossyntBackend.Models {
    public sealed class Tree {
        [NotNull] public TreeNode RootTreeNode { get; }

        /// <summary>
        /// Key is <see cref="TreeNode.NodeId"/> of <see cref="TreeNode"/>.
        /// </summary>
        [NotNull] private readonly Dictionary<string, TreeNode> _treeNodes = new Dictionary<string, TreeNode>();

        /// <summary>
        /// Key is <see cref="TreeNode.NodeId"/> of <see cref="TreeNode"/>.
        /// </summary>
        [NotNull]
        public IReadOnlyDictionary<string, TreeNode> TreeNodes => _treeNodes;

        // ******************************************************************************** //

        private Tree([NotNull] SyntaxNode root) {
            if (root == null) throw new ArgumentNullException(nameof(root));

            var rootTreeNode = AddTreeNode(new TreeNodeSyntaxOrToken(root, null));
            ProcessTreeNode(rootTreeNode);
            RootTreeNode = rootTreeNode;
        }

        [NotNull]
        public static async Task<Tree> CompileFile([NotNull] string fileText, [NotNull] string filePath, CancellationToken cancellationToken) {
            var syntaxTree = CSharpSyntaxTree.ParseText(fileText, path: filePath, cancellationToken: cancellationToken);
            var root = await syntaxTree.GetRootAsync(cancellationToken);
            return new Tree(root);
        }

        private void ProcessTreeNode([NotNull] TreeNodeSyntaxOrToken treeNode) {
            if (treeNode == null) throw new ArgumentNullException(nameof(treeNode));

            // Process leading trivia.
            foreach (var trivia in treeNode.SyntaxNodeOrToken.GetLeadingTrivia()) {
                AddTreeNode(new TreeNodeTrivia(true, trivia, treeNode));
            }

            // Process each child.
            foreach (var child in treeNode.SyntaxNodeOrToken.ChildNodesAndTokens()) {
                ProcessTreeNode(AddTreeNode(new TreeNodeSyntaxOrToken(child, treeNode)));
            }

            // Process trailing trivia.
            foreach (var trivia in treeNode.SyntaxNodeOrToken.GetTrailingTrivia()) {
                AddTreeNode(new TreeNodeTrivia(false, trivia, treeNode));
            }
        }

        [NotNull]
        private TTreeNode AddTreeNode<TTreeNode>([NotNull] TTreeNode treeNode) where TTreeNode : TreeNode {
            _treeNodes.Add(treeNode.NodeId, treeNode);
            return treeNode;
        }
    }
}
