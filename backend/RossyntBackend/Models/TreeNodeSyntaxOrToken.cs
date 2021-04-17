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

        [Pure]
        public override SyntaxKind SyntaxKind() => SyntaxNodeOrToken.Kind();

        [Pure]
        public override TreeNodeCategory TreeNodeCategory() => SyntaxNodeOrToken.IsNode ? Models.TreeNodeCategory.SyntaxNode : Models.TreeNodeCategory.SyntaxToken;

        [Pure]
        [NotNull]
        public override string RawString() => SyntaxNodeOrToken.ToString();

        [Pure]
        [NotNull]
        public override Type RawType() => RawObject().GetType();

        [Pure]
        public override bool IsMissing() => SyntaxNodeOrToken.IsMissing;

        [Pure]
        [NotNull]
        public override IReadOnlyDictionary<string, string> RawProperties() {
            var basicProperties = ObjectUtil.GetObjectProperties(SyntaxNodeOrToken);
            var moreProperties = ObjectUtil.GetObjectProperties(RawObject());

            var rawProperties = new Dictionary<string, string>(basicProperties);
            foreach (var (moreKey, moreValue) in moreProperties) {
                rawProperties[moreKey] = moreValue;
            }

            return rawProperties;
        }

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeCategory()}) {SyntaxNodeOrToken}";

        [Pure]
        [NotNull]
        private object RawObject() {
            return SyntaxNodeOrToken.IsNode ? (object) (SyntaxNodeOrToken.AsNode() ?? throw new InvalidOperationException($"{nameof(SyntaxNodeOrToken.AsNode)}() is null.")) : SyntaxNodeOrToken.AsToken();
        }
    }
}
