using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using AutoFixture;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.CodeAnalysis.CSharp;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using NUnit.Framework;
using RossyntBackend;

#nullable enable

namespace RossyntBackendIntegrationTest {
    [Parallelizable(ParallelScope.All)]
    public class IntegrationTest {
        private readonly Fixture _fixture = new Fixture();

        // ******************************************************************************** //

        [Test]
        public Task CompileFile_EmptyFile() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "", "", false, 1);
            AssertNode(root["Child"]?[0], "SyntaxToken", "SyntaxToken", "EndOfFileToken", "", "", false, 0);
        });

        [Test]
        public Task CompileFile() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "using\r\n");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "using\r\n", "0,7", false, 2);
            AssertNode(root["Child"]?[0], "SyntaxNode", "UsingDirectiveSyntax", "UsingDirective", "using\r\n", "0,7", false, 3);
            AssertNode(root["Child"]?[0]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "UsingKeyword", "using", "0,5", false, 1);
            AssertNode(root["Child"]?[0]?["Child"]?[0]?["Child"]?[0], "TrailingTrivia", "SyntaxTrivia", "EndOfLineTrivia", "\r\n", "5,2", false, 0);
            AssertNode(root["Child"]?[0]?["Child"]?[1], "SyntaxNode", "IdentifierNameSyntax", "IdentifierName", "", "", true, 1);
            AssertNode(root["Child"]?[0]?["Child"]?[1]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "IdentifierToken", "", "", true, 0);
            AssertNode(root["Child"]?[0]?["Child"]?[2], "SyntaxToken", "SyntaxToken", "SemicolonToken", "", "", true, 0);
            AssertNode(root["Child"]?[1], "SyntaxToken", "SyntaxToken", "EndOfFileToken", "", "", false, 0);
        });

        [Test]
        public Task CompileFileCSharp9Record([Values] LanguageVersion cSharpVersion) => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "record R;", cSharpVersion);
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "record R;", "0,9", false, 2);

            switch (cSharpVersion) {
                case LanguageVersion.CSharp1:
                case LanguageVersion.CSharp2:
                case LanguageVersion.CSharp3:
                case LanguageVersion.CSharp4:
                case LanguageVersion.CSharp5:
                case LanguageVersion.CSharp6:
                case LanguageVersion.CSharp7:
                case LanguageVersion.CSharp7_1:
                case LanguageVersion.CSharp7_2:
                case LanguageVersion.CSharp7_3:
                case LanguageVersion.CSharp8:
                    AssertNode(root["Child"]?[0], "SyntaxNode", "GlobalStatementSyntax", "GlobalStatement", "record R;", "0,9", false, 1);
                    break;

                case LanguageVersion.CSharp9:
                case LanguageVersion.CSharp10:
                case LanguageVersion.LatestMajor:
                case LanguageVersion.Preview:
                case LanguageVersion.Latest:
                case LanguageVersion.Default:
                    AssertNode(root["Child"]?[0], "SyntaxNode", "RecordDeclarationSyntax", "RecordDeclaration", "record R;", "0,9", false, 3);
                    break;

                default:
                    throw new ArgumentOutOfRangeException(nameof(cSharpVersion), cSharpVersion, null);
            }
        });

        [Test]
        public Task CompileFileCSharp10GlobalUsingDirectives() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "global using System;");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "global using System;", "0,20", false, 2);
            AssertNode(root["Child"]?[0], "SyntaxNode", "UsingDirectiveSyntax", "UsingDirective", "global using System;", "0,20", false, 4);
            AssertNode(root["Child"]?[0]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "GlobalKeyword", "global", "0,6", false, 1);
        });

        [Test]
        public Task CompileFileCSharp10FileScopedNamespaceDeclaration() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "namespace MyNamespace;");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "namespace MyNamespace;", "0,22", false, 2);
            AssertNode(root["Child"]?[0], "SyntaxNode", "FileScopedNamespaceDeclarationSyntax", "FileScopedNamespaceDeclaration", "namespace MyNamespace;", "0,22", false, 3);
        });

        [Test]
        public Task CompileFileCSharp11GreaterThanGreaterThanGreaterThanToken() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "1 >>> 3");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "1 >>> 3", "0,7", false, 2);
            AssertNode(root["Child"]?[0]?["Child"]?[0]?["Child"]?[0]?["Child"]?[1], "SyntaxToken", "SyntaxToken", "GreaterThanGreaterThanGreaterThanToken", ">>>", "2,3", false, 1);
        });

        [Test]
        public Task GetNodeInfo() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "using");
            var nodeId = root["Child"]?[0]?["Id"]?.Value<string>();
            Assert.IsNotNull(nodeId);
            var nodeInfo = await GetNodeInfo(httpClient, nodeId!);
            Assert.AreEqual("", nodeInfo["Alias"]);
            Assert.AreEqual("False", nodeInfo["ContainsAnnotations"]);
            Assert.AreEqual("True", nodeInfo["ContainsDiagnostics"]);
            Assert.AreEqual("False", nodeInfo["ContainsDirectives"]);
            Assert.AreEqual("False", nodeInfo["ContainsSkippedText"]);
            Assert.AreEqual("[0..5)", nodeInfo["FullSpan"]);
            Assert.AreEqual("", nodeInfo["GlobalKeyword"]);
            Assert.AreEqual("False", nodeInfo["HasLeadingTrivia"]);
            Assert.AreEqual("False", nodeInfo["HasStructuredTrivia"]);
            Assert.AreEqual("False", nodeInfo["HasTrailingTrivia"]);
            Assert.AreEqual("False", nodeInfo["IsMissing"]);
            Assert.AreEqual("True", nodeInfo["IsNode"]);
            Assert.AreEqual("False", nodeInfo["IsStructuredTrivia"]);
            Assert.AreEqual("False", nodeInfo["IsToken"]);
            Assert.AreEqual("C#", nodeInfo["Language"]);
            Assert.AreEqual("", nodeInfo["Name"]);
            Assert.AreEqual("using", nodeInfo["Parent"]);
            Assert.AreEqual("", nodeInfo["ParentTrivia"]);
            Assert.AreEqual("8843", nodeInfo["RawKind"]);
            Assert.AreEqual("", nodeInfo["SemicolonToken"]);
            Assert.AreEqual("[0..5)", nodeInfo["Span"]);
            Assert.AreEqual("0", nodeInfo["SpanStart"]);
            Assert.AreEqual("", nodeInfo["StaticKeyword"]);
            Assert.AreEqual("using", nodeInfo["SyntaxTree"]);
            Assert.AreEqual("using", nodeInfo["UsingKeyword"]);
            Assert.AreEqual(25, nodeInfo.Count);
        });

        [Test]
        public Task ResetActiveFile() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "using");
            var nodeId = root["Id"]?.Value<string>();
            Assert.IsNotNull(nodeId);
            var nodeInfo = await GetNodeInfo(httpClient, nodeId!);
            Assert.IsTrue(nodeInfo.Count > 0);
            await ResetActiveFile(httpClient);
            Assert.CatchAsync<Exception>(() => GetNodeInfo(httpClient, nodeId!));
        });

        [Test]
        public Task FindNode() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "int dummy;");
            var nodeId = await FindNode(httpClient, 6, 6);
            var node = root["Child"]?[0]; // GlobalStatement
            node = node?["Child"]?[0]; // LocalDeclarationStatement
            node = node?["Child"]?[0]; // VariableDeclaration
            node = node?["Child"]?[1]; // VariableDeclarator
            node = node?["Child"]?[0]; // IdentifierToken
            Assert.AreEqual(nodeId, node?["Id"]?.Value<string>());
        });

        private static async Task RunWithHttpClient(Func<HttpClient, Task> func) {
            if (func == null) throw new ArgumentNullException(nameof(func));

            using var webApplicationFactory = new WebApplicationFactory<Startup>();
            var httpClient = webApplicationFactory.CreateClient();
            await func(httpClient);
        }

        private async Task<JObject> CompileFile(HttpClient httpClient, string fileText, LanguageVersion? cSharpVersion = null) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));
            if (fileText == null) throw new ArgumentNullException(nameof(fileText));

            var parameters = ImmutableDictionary<string, string>.Empty;
            parameters = parameters.Add("FilePath", _fixture.Create<string>());
            parameters = parameters.Add("FileText", fileText);
            if (cSharpVersion != null) {
                parameters = parameters.Add("CSharpVersion", cSharpVersion.Value.ToString());
            }

            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/compileFile", new FormUrlEncodedContent(parameters!));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
            var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
            return JObject.Parse(responseBody);
        }

        private static async Task<IDictionary<string, string>> GetNodeInfo(HttpClient httpClient, string nodeId) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));
            if (nodeId == null) throw new ArgumentNullException(nameof(nodeId));

            var parameters = ImmutableDictionary<string, string>.Empty;
            parameters = parameters.Add("NodeId", nodeId);
            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/getNodeInfo", new FormUrlEncodedContent(parameters!));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
            var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
            var dictionary = JsonConvert.DeserializeObject<IDictionary<string, string>>(responseBody);
            Assert.IsNotNull(dictionary);
            return dictionary!;
        }

        private static async Task ResetActiveFile(HttpClient httpClient) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));

            var parameters = ImmutableDictionary<string, string>.Empty;
            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/resetActiveFile", new FormUrlEncodedContent(parameters!));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
        }

        private static async Task<string> FindNode(HttpClient httpClient, int start, int end) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));

            var parameters = ImmutableDictionary<string, string>.Empty;
            parameters = parameters.Add("Start", start.ToString());
            parameters = parameters.Add("End", end.ToString());
            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/findNode", new FormUrlEncodedContent(parameters!));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
            var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
            var dictionary = JsonConvert.DeserializeObject<IDictionary<string, string>>(responseBody);
            Assert.IsNotNull(dictionary);
            Assert.AreEqual(1, dictionary!.Count);
            Assert.IsTrue(dictionary.ContainsKey("nodeId"));
            var nodeId = dictionary["nodeId"];
            Assert.IsNotNull(nodeId);
            return nodeId;
        }

        private static void AssertNode(JToken? node, string cat, string type, string kind, string str, string span, bool isMissing, int childCount) {
            if (cat == null) throw new ArgumentNullException(nameof(cat));
            if (type == null) throw new ArgumentNullException(nameof(type));
            if (kind == null) throw new ArgumentNullException(nameof(kind));
            if (str == null) throw new ArgumentNullException(nameof(str));
            if (span == null) throw new ArgumentNullException(nameof(span));

            Assert.IsNotNull(node);
            Assert.AreEqual(cat, node!["Cat"]?.Value<string>());
            Assert.AreEqual(type, node["Type"]?.Value<string>());
            Assert.AreEqual(kind, node["Kind"]?.Value<string>());
            Assert.AreEqual(str.Length > 0 ? str : null, node["Str"]?.Value<string>());
            Assert.AreEqual(span.Length > 0 ? span : null, node["Span"]?.Value<string>());
            Assert.AreEqual(isMissing ? (int?)1 : null, node["IsMissing"]?.Value<int>());
            Assert.AreEqual(childCount > 0 ? (int?)childCount : null, node["Child"]?.Count());
        }
    }
}
