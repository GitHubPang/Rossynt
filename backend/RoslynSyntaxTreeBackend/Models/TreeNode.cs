using System;
using JetBrains.Annotations;
using Microsoft.CodeAnalysis;

namespace RoslynSyntaxTreeBackend.Models {
    public sealed class TreeNode {
        [NotNull] public string NodeId { get; } = Guid.NewGuid().ToString("D");
        public SyntaxNodeOrToken SyntaxNodeOrToken { get; }

        // ******************************************************************************** //

        public TreeNode(SyntaxNodeOrToken syntaxNodeOrToken) {
            SyntaxNodeOrToken = syntaxNodeOrToken;
        }
    }
}
