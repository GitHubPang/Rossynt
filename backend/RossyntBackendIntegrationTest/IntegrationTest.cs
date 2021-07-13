using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using AutoFixture;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc.Testing;
using Newtonsoft.Json.Linq;
using NUnit.Framework;
using RossyntBackend;

namespace RossyntBackendIntegrationTest {
    [Parallelizable(ParallelScope.All)]
    public class IntegrationTest {
        [NotNull] private readonly Fixture _fixture = new Fixture();

        // ******************************************************************************** //

        [Test]
        public async Task TestEmptyFile() {
            using (var webApplicationFactory = new WebApplicationFactory<Startup>()) {
                var httpClient = webApplicationFactory.CreateClient();
                var parameters = new Dictionary<string, string> {
                    ["FilePath"] = _fixture.Create<string>(),
                    ["FileText"] = "",
                };
                var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/compileFile", new FormUrlEncodedContent(parameters));
                Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
                var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
                var root = JObject.Parse(responseBody);
                AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "", "", false, 1);
                AssertNode(root["Child"]?[0], "SyntaxToken", "SyntaxToken", "EndOfFileToken", "", "", false, 0);
            }
        }

        [Test]
        public async Task TestBasic() {
            using (var webApplicationFactory = new WebApplicationFactory<Startup>()) {
                var httpClient = webApplicationFactory.CreateClient();
                var parameters = new Dictionary<string, string> {
                    ["FilePath"] = _fixture.Create<string>(),
                    ["FileText"] = "using\r\n",
                };
                var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/compileFile", new FormUrlEncodedContent(parameters));
                Assert.IsTrue(httpResponseMessage.IsSuccessStatusCode);
                var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
                var root = JObject.Parse(responseBody);
                AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "using\r\n", "0,7", false, 2);
                AssertNode(root["Child"]?[0], "SyntaxNode", "UsingDirectiveSyntax", "UsingDirective", "using\r\n", "0,7", false, 3);
                AssertNode(root["Child"]?[0]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "UsingKeyword", "using", "0,5", false, 1);
                AssertNode(root["Child"]?[0]?["Child"]?[0]?["Child"]?[0], "TrailingTrivia", "SyntaxTrivia", "EndOfLineTrivia", "\r\n", "5,2", false, 0);
                AssertNode(root["Child"]?[0]?["Child"]?[1], "SyntaxNode", "IdentifierNameSyntax", "IdentifierName", "", "", true, 1);
                AssertNode(root["Child"]?[0]?["Child"]?[1]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "IdentifierToken", "", "", true, 0);
                AssertNode(root["Child"]?[0]?["Child"]?[2], "SyntaxToken", "SyntaxToken", "SemicolonToken", "", "", true, 0);
                AssertNode(root["Child"]?[1], "SyntaxToken", "SyntaxToken", "EndOfFileToken", "", "", false, 0);
            }
        }

        private static void AssertNode([CanBeNull] JToken node, [NotNull] string cat, [NotNull] string type, [NotNull] string kind, [NotNull] string str, [NotNull] string span, bool isMissing, int childCount) {
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
