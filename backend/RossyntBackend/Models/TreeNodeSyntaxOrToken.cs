using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

namespace RossyntBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNodeSyntaxOrToken : TreeNode {
        public SyntaxNodeOrToken SyntaxNodeOrToken { get; }

        // ******************************************************************************** //

        public TreeNodeSyntaxOrToken(SyntaxNodeOrToken syntaxNodeOrToken, [CanBeNull] TreeNode parentTreeNode) : base(parentTreeNode) => SyntaxNodeOrToken = syntaxNodeOrToken;
        public override SyntaxKind Kind() => SyntaxNodeOrToken.Kind();
        public override TreeNodeType TreeNodeType() => SyntaxNodeOrToken.IsNode ? Models.TreeNodeType.SyntaxNode : Models.TreeNodeType.SyntaxToken;

        [Pure]
        [NotNull]
        public override string ShortString() => SyntaxNodeOrToken.ToString();

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeType()}) {SyntaxNodeOrToken}";
    }
}
