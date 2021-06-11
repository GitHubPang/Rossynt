using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.Text;

namespace RossyntBackend.Models {
    public sealed class Tree {
        [NotNull] private readonly SyntaxNode _rootSyntaxNode;
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
            _rootSyntaxNode = root ?? throw new ArgumentNullException(nameof(root));

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

        [CanBeNull]
        public TreeNode FindTreeNode(TextSpan textSpan) {
            // Skip if out of range.
            if (!_rootSyntaxNode.FullSpan.Contains(textSpan)) {
                return null;
            }

            // Find trivia.
            {
                var syntaxTriviaStart = _rootSyntaxNode.FindTrivia(textSpan.Start);
                var syntaxTriviaEnd = textSpan.End - 1 > textSpan.Start ? _rootSyntaxNode.FindTrivia(textSpan.End - 1) : syntaxTriviaStart;
                if (syntaxTriviaStart == syntaxTriviaEnd) {
                    var treeNode = _treeNodes.Values.OfType<TreeNodeTrivia>().FirstOrDefault(_ => _.SyntaxTrivia == syntaxTriviaStart);
                    if (treeNode != null) {
                        return treeNode;
                    }
                }
            }

            // Find token.
            {
                var syntaxTokenStart = _rootSyntaxNode.FindToken(textSpan.Start, true);
                var syntaxTokenEnd = textSpan.End - 1 > textSpan.Start ? _rootSyntaxNode.FindToken(textSpan.End - 1, true) : syntaxTokenStart;
                if (syntaxTokenStart == syntaxTokenEnd) {
                    var treeNode = _treeNodes.Values.OfType<TreeNodeSyntaxOrToken>().FirstOrDefault(_ => _.SyntaxNodeOrToken.AsToken() == syntaxTokenStart);
                    if (treeNode != null) {
                        return treeNode;
                    }
                }
            }

            // Find node.
            {
                var syntaxNode = _rootSyntaxNode.FindNode(textSpan, getInnermostNodeForTie: true);
                return _treeNodes.Values.OfType<TreeNodeSyntaxOrToken>().FirstOrDefault(_ => _.SyntaxNodeOrToken.AsNode() == syntaxNode);
            }
        }
    }
}
