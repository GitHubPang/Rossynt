using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.Text;

namespace RossyntBackend.Models {
    public abstract class TreeNode {
        [NotNull] public string NodeId { get; } = Guid.NewGuid().ToString("D");

        // ReSharper disable once MemberCanBePrivate.Global UnusedAutoPropertyAccessor.Global
        [CanBeNull] public TreeNode ParentTreeNode { get; }

        [NotNull] private readonly List<TreeNode> _childTreeNodes = new List<TreeNode>();

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        [NotNull]
        // ReSharper disable once ReturnTypeCanBeEnumerable.Global
        public IReadOnlyCollection<TreeNode> ChildTreeNodes => _childTreeNodes;

        // ******************************************************************************** //

        protected TreeNode([CanBeNull] TreeNode parentTreeNode) {
            ParentTreeNode = parentTreeNode;

            // Add as parent's child.
            parentTreeNode?._childTreeNodes.Add(this);
        }

        [Pure]
        public abstract SyntaxKind SyntaxKind();

        [Pure]
        public abstract TreeNodeCategory TreeNodeCategory();

        [Pure]
        [NotNull]
        public abstract string RawString();

        [Pure]
        [NotNull]
        public abstract Type RawType();

        [Pure]
        public abstract TextSpan Span();

        [Pure]
        public abstract bool IsMissing();

        [Pure]
        [NotNull]
        public abstract IReadOnlyDictionary<string, string> RawProperties();
    }
}
