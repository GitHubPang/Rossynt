using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.Text;
using RossyntBackend.Utils;

#nullable enable

namespace RossyntBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNodeSyntaxOrToken : TreeNode {
        public SyntaxNodeOrToken SyntaxNodeOrToken { get; }

        // ******************************************************************************** //

        public TreeNodeSyntaxOrToken(SyntaxNodeOrToken syntaxNodeOrToken, TreeNode? parentTreeNode) : base(parentTreeNode) => SyntaxNodeOrToken = syntaxNodeOrToken;

        [Pure]
        public override SyntaxKind SyntaxKind() => SyntaxNodeOrToken.Kind();

        [Pure]
        public override TreeNodeCategory TreeNodeCategory() => SyntaxNodeOrToken.IsNode ? Models.TreeNodeCategory.SyntaxNode : Models.TreeNodeCategory.SyntaxToken;

        [Pure]
        public override string RawString() => SyntaxNodeOrToken.ToString();

        [Pure]
        public override Type RawType() => RawObject().GetType();

        [Pure]
        public override TextSpan Span() => SyntaxNodeOrToken.Span;

        [Pure]
        public override bool IsMissing() => SyntaxNodeOrToken.IsMissing;

        [Pure]
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
        private object RawObject() {
            return SyntaxNodeOrToken.IsNode ? (object)(SyntaxNodeOrToken.AsNode() ?? throw new InvalidOperationException($"{nameof(SyntaxNodeOrToken.AsNode)}() is null.")) : SyntaxNodeOrToken.AsToken();
        }
    }
}
