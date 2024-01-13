using System;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using Microsoft.CodeAnalysis.CSharp;
using NUnit.Framework;

namespace RossyntBackendUnitTest;

public class CSharpVersionTest {
    /// <summary>
    /// Note: must sync with Microsoft.CodeAnalysis.CSharp.LanguageVersion and front end.
    /// </summary>
    [SuppressMessage("ReSharper", "InconsistentNaming")]
    [SuppressMessage("ReSharper", "UnusedMember.Local")]
    private enum CSharpVersion {
        Default,
        CSharp1,
        CSharp2,
        CSharp3,
        CSharp4,
        CSharp5,
        CSharp6,
        CSharp7,
        CSharp7_1,
        CSharp7_2,
        CSharp7_3,
        CSharp8,
        CSharp9,
        CSharp10,
        CSharp11,
        CSharp12,
        Latest,
        LatestMajor,
        Preview
    }

    [Test]
    public void CSharpVersion_ArgumentNullException() {
        var languageVersions = Enum.GetValues<LanguageVersion>();
        var cSharpVersions = Enum.GetValues<CSharpVersion>();
        Assert.That(languageVersions.Select(_ => _.ToString()), Is.EquivalentTo(cSharpVersions.Select(_ => _.ToString())));
    }
}
