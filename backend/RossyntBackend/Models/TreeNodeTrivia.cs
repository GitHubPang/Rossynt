using System;
using System.Collections.Generic;
using System.Diagnostics;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using RossyntBackend.Utils;

namespace RossyntBackend.Models {
    [DebuggerDisplay("{DebuggerDisplay,nq}")]
    public sealed class TreeNodeTrivia : TreeNode {
        private readonly TreeNodeCategory _treeNodeCategory;
        private readonly SyntaxTrivia _syntaxTrivia;

        // ******************************************************************************** //

        public TreeNodeTrivia(bool isLeading, SyntaxTrivia syntaxTrivia, [CanBeNull] TreeNode parentTreeNode) : base(parentTreeNode) {
            _treeNodeCategory = isLeading ? Models.TreeNodeCategory.LeadingTrivia : Models.TreeNodeCategory.TrailingTrivia;
            _syntaxTrivia = syntaxTrivia;
        }

        [Pure]
        public override SyntaxKind SyntaxKind() => _syntaxTrivia.Kind();

        [Pure]
        public override TreeNodeCategory TreeNodeCategory() => _treeNodeCategory;

        [Pure]
        [NotNull]
        public override string RawString() => _syntaxTrivia.ToString();

        [Pure]
        [NotNull]
        public override Type RawType() => _syntaxTrivia.GetType();

        [Pure]
        [NotNull]
        public override IReadOnlyDictionary<string, string> RawProperties() => ObjectUtil.GetObjectProperties(_syntaxTrivia);

        [DebuggerBrowsable(DebuggerBrowsableState.Never)]
        public string DebuggerDisplay => $"({TreeNodeCategory()}) {_syntaxTrivia}";
    }
}
