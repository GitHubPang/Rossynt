using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;

namespace RoslynSyntaxTreeBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNode {
        [NotNull] public string NodeId { get; } = Guid.NewGuid().ToString("D");
        public SyntaxNodeOrToken SyntaxNodeOrToken { get; }

        // ReSharper disable once MemberCanBePrivate.Global UnusedAutoPropertyAccessor.Global
        [CanBeNull] public TreeNode ParentTreeNode { get; }

        [NotNull] private readonly List<TreeNode> _childTreeNodes = new List<TreeNode>();

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        [NotNull]
        // ReSharper disable once ReturnTypeCanBeEnumerable.Global
        public IReadOnlyCollection<TreeNode> ChildTreeNodes => _childTreeNodes;

        // ******************************************************************************** //

        public TreeNode(SyntaxNodeOrToken syntaxNodeOrToken, [CanBeNull] TreeNode parentTreeNode) {
            ParentTreeNode = parentTreeNode;
            SyntaxNodeOrToken = syntaxNodeOrToken;

            // Add as parent's child.
            parentTreeNode?._childTreeNodes.Add(this);
        }

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => SyntaxNodeOrToken.ToString();
    }
}
