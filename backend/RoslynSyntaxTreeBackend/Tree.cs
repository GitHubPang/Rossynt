using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

#nullable enable

namespace RoslynSyntaxTreeBackend {
    public sealed class Tree {
        private readonly SyntaxTree _syntaxTree;
        private readonly SyntaxNode _root;

        // ******************************************************************************** //

        private Tree(SyntaxTree syntaxTree, SyntaxNode root) {
            _syntaxTree = syntaxTree ?? throw new ArgumentNullException(nameof(syntaxTree));
            _root = root ?? throw new ArgumentNullException(nameof(root));
        }

        public static async Task<Tree> CompileFile(string filePath, CancellationToken cancellationToken) {
            var fileContent = await File.ReadAllTextAsync(filePath, cancellationToken);
            var syntaxTree = CSharpSyntaxTree.ParseText(fileContent, path: filePath, cancellationToken: cancellationToken);
            var root = await syntaxTree.GetRootAsync(cancellationToken);
            return new Tree(syntaxTree, root);
        }
    }
}
