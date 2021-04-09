using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using RossyntBackend.Utils;

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

        [Pure]
        [NotNull]
        public override IReadOnlyDictionary<string, string> RawProperties() {
            var basicProperties = ObjectUtil.GetObjectProperties(SyntaxNodeOrToken);
            var moreProperties = ObjectUtil.GetObjectProperties(SyntaxNodeOrToken.IsNode ? (object) (SyntaxNodeOrToken.AsNode() ?? throw new InvalidOperationException("AsNode() is null.")) : SyntaxNodeOrToken.AsToken());

            var rawProperties = new Dictionary<string, string>(basicProperties);
            foreach (var (moreKey, moreValue) in moreProperties) {
                rawProperties[moreKey] = moreValue;
            }

            return rawProperties;
        }

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeType()}) {SyntaxNodeOrToken}";
    }
}
