<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Rossynt Changelog

## [Unreleased]
### Fixed
- Fix #86, caused by threading issues - 0a406c6c31e02056a7a0501202f80b0912c05c31

## [213.12.0] - 2022-02-22
### Changed
- Updated [`Microsoft.CodeAnalysis.CSharp`](https://www.nuget.org/packages/Microsoft.CodeAnalysis.CSharp) (Roslyn) from 4.0.1 to 4.1.0. Now it recognizes C# 11 stuff such as [`ExclamationExclamationToken`](https://docs.microsoft.com/en-us/dotnet/api/microsoft.codeanalysis.csharp.syntaxkind?view=roslyn-dotnet-4.1.0#fields) - 82128cfe9a24d1ca016db19b93216fc3585d1b0c, 0be63f72cd34e9276bd8c227bdf17abb9b46561c

## [213.11.0] - 2022-01-26
### Changed
- Disable the "Select Node at Caret" button whenever the tree is empty - c26720c63159fae45d704415e5c372f1bdd445f2

## [213.10.0] - 2022-01-06
### Added
- Support .NET 6.

### Removed
- Dropped support for .NET Core 2.1, which [has already reached end of support on 2021-08-21](https://dotnet.microsoft.com/en-us/platform/support/policy/dotnet-core).

## [213.9.0] - 2021-12-23
### Added
- Recognize a few more syntax kinds as error: `BadDirectiveTrivia`, `UnknownAccessorDeclaration`, `IncompleteMember` - 40e352100d4e1437adaedba290b079add3d8ff3f

## [213.8.0] - 2021-12-13
### Added
- Show error icon in table - ccb9703a6d9f30d89ccd097a92822c0e0383c409

## [213.7.1] - 2021-12-03
### Changed
- Improve table look and feel - da1f6d7985c37e72a587fc6e7fdc3765ef4697c3

## [213.7.0] - 2021-11-23
### Added
- Added speed search for table - 2742f7da233a97535d3caa8702e3febce33a5709

## [213.6.0] - 2021-11-16
### Changed
- Updated [`Microsoft.CodeAnalysis.CSharp`](https://www.nuget.org/packages/Microsoft.CodeAnalysis.CSharp) (Roslyn) from 3.11.0 to 4.0.1. Now it recognizes more C# 10 stuff, such as [`FileScopedNamespaceDeclarationSyntax`](https://docs.microsoft.com/en-us/dotnet/api/microsoft.codeanalysis.csharp.syntax.filescopednamespacedeclarationsyntax), which is for [file-scoped namespace declaration](https://docs.microsoft.com/en-us/dotnet/csharp/whats-new/csharp-10#file-scoped-namespace-declaration) - 2d792457bba46a480c5bdda7d4f6da9134f14792

## [203.5.0] - 2021-10-29
### Added
- Support IntelliJ Platform 2021.3 - 422f80a6b77d54f482494b40135f19b060da5893

## [203.4.0] - 2021-10-20
### Added
- Added speed search for tree - 65d2de8ebd481af14218fd452940ab2daf2a0ede, 5e5129c98f52213a799277d1de0425ff30434c70

### Changed
- Make tree nodes copy-paste friendly - 5e5129c98f52213a799277d1de0425ff30434c70

## [203.3.1] - 2021-09-20
### Changed
- Updated Roslyn from 3.10.0 to 3.11.0, which leads to new stuff such as the [`UsingDirectiveSyntax.GlobalKeyword`](https://docs.microsoft.com/en-us/dotnet/api/microsoft.codeanalysis.csharp.syntax.usingdirectivesyntax.globalkeyword) property - 53ecb15ddd7d148526f61a969548e1bb8ef95e0f

## [203.3.0] - 2021-09-07
### Changed
- Take [line separator](https://www.jetbrains.com/help/idea/configuring-line-endings-and-line-separators.html#line_ending) of the source file into account. In other words, those `EndOfLineTrivia` nodes would have a `Span.Length` of 2 instead of 1 when the line separator is `CRLF` (`\r\n`).

## [203.2.0] - 2021-08-18
### Added
- Added recognizing default location of dotnet executable on macOS as installed by Rider - 362c0e20edb42e8a802d1e48293eb3af405a4b46

### Fixed
- Fixed incorrect default location of dotnet executable on Windows as installed by Rider - ca91f6f5c1f57612775fc3141b65d37850146c53

## [203.1.0] - 2021-08-11
### Added
- Added shortcut key for the Collapse All action for the tree view - a464721a83ddea68b7e3b54753a9f1e4e24cf98f

### Changed
- After Collapse All in tree view, automatically expand the root node - eb8d2fde416f30a76e251ae2a4e8608378660345

## [203.0.0] - 2021-07-27
### Changed
- Support IntelliJ Platform 2021.2.

## [202.3.0] - 2021-07-20
### Added
- Compile file even if it's empty or whitespace only.

## [202.2.1] - 2021-07-05
### Fixed
- Do not refresh data when tool window is hidden, thus improving performance.

## [202.2.0] - 2021-06-16
### Added
- Added a button to select the node in the tree which corresponds to the selection in the text editor.

### Changed
- Reordered the text highlight layers.

## [1.0.0] - 2021-05-31
### Added
- Initial version.
