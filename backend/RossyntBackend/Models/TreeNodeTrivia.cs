using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RossyntBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNodeTrivia : TreeNode {
        private readonly TreeNodeType _treeNodeType;
        private readonly SyntaxTrivia _syntaxTrivia;

        // ******************************************************************************** //

        public TreeNodeTrivia(bool isLeading, SyntaxTrivia syntaxTrivia, [CanBeNull] TreeNode parentTreeNode) : base(parentTreeNode) {
            _treeNodeType = isLeading ? Models.TreeNodeType.LeadingTrivia : Models.TreeNodeType.TrailingTrivia;
            _syntaxTrivia = syntaxTrivia;
        }

        public override SyntaxKind Kind() => _syntaxTrivia.Kind();
        public override TreeNodeType TreeNodeType() => _treeNodeType;

        [Pure]
        [NotNull]
        public override string ShortString() => _syntaxTrivia.ToString();

        [Pure]
        [NotNull]
        public override object RawObject() => _syntaxTrivia;

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeType()}) {_syntaxTrivia}";
    }
}
