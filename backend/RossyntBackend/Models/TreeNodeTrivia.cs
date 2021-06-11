using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.Text;
using RossyntBackend.Utils;

namespace RossyntBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNodeTrivia : TreeNode {
        private readonly TreeNodeCategory _treeNodeCategory;
        public SyntaxTrivia SyntaxTrivia { get; }

        // ******************************************************************************** //

        public TreeNodeTrivia(bool isLeading, SyntaxTrivia syntaxTrivia, [CanBeNull] TreeNode parentTreeNode) : base(parentTreeNode) {
            _treeNodeCategory = isLeading ? Models.TreeNodeCategory.LeadingTrivia : Models.TreeNodeCategory.TrailingTrivia;
            SyntaxTrivia = syntaxTrivia;
        }

        [Pure]
        public override SyntaxKind SyntaxKind() => SyntaxTrivia.Kind();

        [Pure]
        public override TreeNodeCategory TreeNodeCategory() => _treeNodeCategory;

        [Pure]
        [NotNull]
        public override string RawString() => SyntaxTrivia.ToString();

        [Pure]
        [NotNull]
        public override Type RawType() => SyntaxTrivia.GetType();

        [Pure]
        public override TextSpan Span() => SyntaxTrivia.Span;

        [Pure]
        public override bool IsMissing() => false;

        [Pure]
        [NotNull]
        public override IReadOnlyDictionary<string, string> RawProperties() => ObjectUtil.GetObjectProperties(SyntaxTrivia);

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeCategory()}) {SyntaxTrivia}";
    }
}
