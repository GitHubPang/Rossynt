<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Rossynt Changelog

## [Unreleased]
### Added
- Added speed search for tree - 65d2de8ebd481af14218fd452940ab2daf2a0ede, 5e5129c98f52213a799277d1de0425ff30434c70
- Added speed search for table - d47336e26cd8c6a6d9f017ed0786de6d0ca9cfa6
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
