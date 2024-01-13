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

namespace RossyntBackendIntegrationTest;

[Parallelizable(ParallelScope.All)]
public class IntegrationTest {
    private readonly Fixture _fixture = new();

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
            case LanguageVersion.CSharp11:
            case LanguageVersion.CSharp12:
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
    public Task CompileFileCSharp11RawStringLiterals() => RunWithHttpClient(async httpClient => {
        var root = await CompileFile(httpClient, "var x = $\"\"\"\n  y {{v}}\n  \"\"\"");
        AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "var x = $\"\"\"\n  y {{v}}\n  \"\"\"", "0,28", false, 2);
        AssertNode(root["Child"]?[0]?["Child"]?[0]?["Child"]?[0]?["Child"]?[1]?["Child"]?[1]?["Child"]?[1]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "InterpolatedMultiLineRawStringStartToken", "$\"\"\"\n", "8,5", false, 0);
    });

    [Test]
    public Task CompileFileCSharp11FileScopedType() => RunWithHttpClient(async httpClient => {
        var root = await CompileFile(httpClient, "file class C");
        AssertNode(root, "SyntaxNode", "CompilationUnitSyntax", "CompilationUnit", "file class C", "0,12", false, 2);
        AssertNode(root["Child"]?[0]?["Child"]?[0], "SyntaxToken", "SyntaxToken", "FileKeyword", "file", "0,4", false, 1);
    });

    [Test]
    public Task GetNodeInfo() => RunWithHttpClient(async httpClient => {
        var root = await CompileFile(httpClient, "using");
        var nodeId = root["Child"]?[0]?["Id"]?.Value<string>();
        Assert.That(nodeId, Is.Not.Null);
        var nodeInfo = await GetNodeInfo(httpClient, nodeId!);
        Assert.That(nodeInfo["Alias"], Is.EqualTo(""));
        Assert.That(nodeInfo["ContainsAnnotations"], Is.EqualTo("False"));
        Assert.That(nodeInfo["ContainsDiagnostics"], Is.EqualTo("True"));
        Assert.That(nodeInfo["ContainsDirectives"], Is.EqualTo("False"));
        Assert.That(nodeInfo["ContainsSkippedText"], Is.EqualTo("False"));
        Assert.That(nodeInfo["FullSpan"], Is.EqualTo("[0..5)"));
        Assert.That(nodeInfo["GlobalKeyword"], Is.EqualTo(""));
        Assert.That(nodeInfo["HasLeadingTrivia"], Is.EqualTo("False"));
        Assert.That(nodeInfo["HasStructuredTrivia"], Is.EqualTo("False"));
        Assert.That(nodeInfo["HasTrailingTrivia"], Is.EqualTo("False"));
        Assert.That(nodeInfo["IsMissing"], Is.EqualTo("False"));
        Assert.That(nodeInfo["IsNode"], Is.EqualTo("True"));
        Assert.That(nodeInfo["IsStructuredTrivia"], Is.EqualTo("False"));
        Assert.That(nodeInfo["IsToken"], Is.EqualTo("False"));
        Assert.That(nodeInfo["Language"], Is.EqualTo("C#"));
        Assert.That(nodeInfo["Name"], Is.EqualTo(""));
        Assert.That(nodeInfo["NamespaceOrType"], Is.EqualTo(""));
        Assert.That(nodeInfo["Parent"], Is.EqualTo("using"));
        Assert.That(nodeInfo["ParentTrivia"], Is.EqualTo(""));
        Assert.That(nodeInfo["RawKind"], Is.EqualTo("8843"));
        Assert.That(nodeInfo["SemicolonToken"], Is.EqualTo(""));
        Assert.That(nodeInfo["Span"], Is.EqualTo("[0..5)"));
        Assert.That(nodeInfo["SpanStart"], Is.EqualTo("0"));
        Assert.That(nodeInfo["StaticKeyword"], Is.EqualTo(""));
        Assert.That(nodeInfo["SyntaxTree"], Is.EqualTo("using"));
        Assert.That(nodeInfo["UnsafeKeyword"], Is.EqualTo(""));
        Assert.That(nodeInfo["UsingKeyword"], Is.EqualTo("using"));
        Assert.That(nodeInfo.Count, Is.EqualTo(27));
    });

    [Test]
    public Task ResetActiveFile() => RunWithHttpClient(async httpClient => {
        var root = await CompileFile(httpClient, "using");
        var nodeId = root["Id"]?.Value<string>();
        Assert.That(nodeId, Is.Not.Null);
        var nodeInfo = await GetNodeInfo(httpClient, nodeId!);
        Assert.That(nodeInfo.Count, Is.GreaterThan(0));
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
        Assert.That(nodeId, Is.EqualTo(node?["Id"]?.Value<string>()));
    });

    private static async Task RunWithHttpClient(Func<HttpClient, Task> func) {
        if (func == null) throw new ArgumentNullException(nameof(func));

        await using var webApplicationFactory = new WebApplicationFactory<Startup>();
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

        var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/compileFile", new FormUrlEncodedContent(parameters));
        Assert.That(httpResponseMessage.IsSuccessStatusCode, Is.True);
        var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
        return JObject.Parse(responseBody);
    }

    private static async Task<IDictionary<string, string>> GetNodeInfo(HttpClient httpClient, string nodeId) {
        if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));
        if (nodeId == null) throw new ArgumentNullException(nameof(nodeId));

        var parameters = ImmutableDictionary<string, string>.Empty;
        parameters = parameters.Add("NodeId", nodeId);
        var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/getNodeInfo", new FormUrlEncodedContent(parameters));
        Assert.That(httpResponseMessage.IsSuccessStatusCode, Is.True);
        var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
        var dictionary = JsonConvert.DeserializeObject<IDictionary<string, string>>(responseBody);
        Assert.That(dictionary, Is.Not.Null);
        return dictionary!;
    }

    private static async Task ResetActiveFile(HttpClient httpClient) {
        if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));

        var parameters = ImmutableDictionary<string, string>.Empty;
        var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/resetActiveFile", new FormUrlEncodedContent(parameters));
        Assert.That(httpResponseMessage.IsSuccessStatusCode, Is.True);
    }

    private static async Task<string> FindNode(HttpClient httpClient, int start, int end) {
        if (httpClient == null) throw new ArgumentNullException(nameof(httpClient));

        var parameters = ImmutableDictionary<string, string>.Empty;
        parameters = parameters.Add("Start", start.ToString());
        parameters = parameters.Add("End", end.ToString());
        var httpResponseMessage = await httpClient.PostAsync("/syntaxTree/findNode", new FormUrlEncodedContent(parameters));
        Assert.That(httpResponseMessage.IsSuccessStatusCode, Is.True);
        var responseBody = await httpResponseMessage.Content.ReadAsStringAsync();
        var dictionary = JsonConvert.DeserializeObject<IDictionary<string, string>>(responseBody);
        Assert.That(dictionary, Is.Not.Null);
        Assert.That(1, Is.EqualTo(dictionary!.Count));
        Assert.That(dictionary.ContainsKey("nodeId"), Is.True);
        var nodeId = dictionary["nodeId"];
        Assert.That(nodeId, Is.Not.Null);
        return nodeId;
    }

    private static void AssertNode(JToken? node, string cat, string type, string kind, string str, string span, bool isMissing, int childCount) {
        if (cat == null) throw new ArgumentNullException(nameof(cat));
        if (type == null) throw new ArgumentNullException(nameof(type));
        if (kind == null) throw new ArgumentNullException(nameof(kind));
        if (str == null) throw new ArgumentNullException(nameof(str));
        if (span == null) throw new ArgumentNullException(nameof(span));

        Assert.That(node, Is.Not.Null);
        Assert.That(node!["Cat"]?.Value<string>(), Is.EqualTo(cat));
        Assert.That(node["Type"]?.Value<string>(), Is.EqualTo(type));
        Assert.That(node["Kind"]?.Value<string>(), Is.EqualTo(kind));
        Assert.That(node["Str"]?.Value<string>(), Is.EqualTo(str.Length > 0 ? str : null));
        Assert.That(node["Span"]?.Value<string>(), Is.EqualTo(span.Length > 0 ? span : null));
        Assert.That(node["IsMissing"]?.Value<int>(), Is.EqualTo(isMissing ? (int?)1 : null));
        Assert.That(node["Child"]?.Count(), Is.EqualTo(childCount > 0 ? (int?)childCount : null));
    }
}
