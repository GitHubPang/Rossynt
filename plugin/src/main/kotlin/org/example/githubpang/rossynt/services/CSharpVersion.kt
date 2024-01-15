package org.example.githubpang.rossynt.services

/**
 * Note: must sync with Microsoft.CodeAnalysis.CSharp.LanguageVersion and backend.
 */
@Suppress("EnumEntryName")
internal enum class CSharpVersion {
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
    Preview,
}
