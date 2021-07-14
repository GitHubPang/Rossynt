using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using AutoFixture;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc.Testing;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using NUnit.Framework;
using RossyntBackend;

namespace RossyntBackendIntegrationTest {
    [Parallelizable(ParallelScope.All)]
    public class IntegrationTest {
        [NotNull] private readonly Fixture _fixture = new Fixture();

        // ******************************************************************************** //

        [Test]
        public Task TestEmptyFile() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "");
            AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "", "", false, 1);
            AssertNode(root["Child"]?[0], "SyntaxToken", "SyntaxToken", "EndOfFileToken", "", "", false, 0);
        });

        [Test]
        public Task TestSimpleFile() => RunWithHttpClient(async httpClient => {
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
        public Task TestGetNodeInfo() => RunWithHttpClient(async httpClient => {
            var root = await CompileFile(httpClient, "using");
            var nodeId = root["Child"]?[0]?["Id"]?.Value<string>();
            Assert.NotNull(nodeId);
            var nodeInfo = await GetNodeInfo(httpClient, nodeId);
            Assert.AreEqual(24, nodeInfo.Count);
            Assert.AreEqual("", nodeInfo["Alias"]);
            Assert.AreEqual("False", nodeInfo["ContainsAnnotations"]);
            Assert.AreEqual("True", nodeInfo["ContainsDiagnostics"]);
            Assert.AreEqual("False", nodeInfo["ContainsDirectives"]);
            Assert.AreEqual("False", nodeInfo["ContainsSkippedText"]);
            Assert.AreEqual("[0..5)", nodeInfo["FullSpan"]);
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
        });

        private static async Task RunWithHttpClient([NotNull] Func<HttpClient, Task> func) {
            if (func == null) throw new ArgumentNullException(nameof(func));
            using (var webApplicationFactory = new WebApplicationFactory<Startup>()) {
                var httpClient = webApplicationFactory.CreateClient();
                await func(httpClient);
            }
        }

        [ItemNotNull]
        private async Task<JObject> CompileFile([NotNull] HttpClient httpClient, [NotNull] string fileText) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));
            if (fileText == null) throw new ArgumentNullException(nameof(fileText));

            var parameters = new Dictionary<string, string> {
                ["FilePath"] = _fixture.Create<string>(),
                ["FileText"] = fileText,
            };
            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/compileFile", new FormUrlEncodedContent(parameters));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
            var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
            return JObject.Parse(responseBody);
        }

        [ItemNotNull]
        private async Task<IDictionary<string, string>> GetNodeInfo([NotNull] HttpClient httpClient, [NotNull] string nodeId) {
            if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));
            if (nodeId == null) throw new ArgumentNullException(nameof(nodeId));

            var parameters = new Dictionary<string, string> {
                ["NodeId"] = nodeId,
            };
            var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/getNodeInfo", new FormUrlEncodedContent(parameters));
            Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
            var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
            var dictionary = JsonConvert.DeserializeObject<IDictionary<string, string>>(responseBody);
            Assert.NotNull(dictionary);
            return dictionary;
        }

        private static void AssertNode([CanBeNull] JToken node, [NotNull] string cat, [NotNull] string type, [NotNull] string kind, [NotNull] string str, [NotNull] string span, bool isMissing, int childCount) {
            if (cat == null) throw new ArgumentNullException(nameof(cat));
            if (type == null) throw new ArgumentNullException(nameof(type));
            if (kind == null) throw new ArgumentNullException(nameof(kind));
            if (str == null) throw new ArgumentNullException(nameof(str));
            if (span == null) throw new ArgumentNullException(nameof(span));

            Assert.NotNull(node);
            Assert.AreEqual(cat, node["Cat"]?.Value<string>());
            Assert.AreEqual(type, node["Type"]?.Value<string>());
            Assert.AreEqual(kind, node["Kind"]?.Value<string>());
            Assert.AreEqual(str.Length > 0 ? str : null, node["Str"]?.Value<string>());
            Assert.AreEqual(span.Length > 0 ? span : null, node["Span"]?.Value<string>());
            Assert.AreEqual(isMissing ? (int?) 1 : null, node["IsMissing"]?.Value<int>());
            Assert.AreEqual(childCount > 0 ? (int?) childCount : null, node["Child"]?.Count());
        }
    }
}
