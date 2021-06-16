# Rossynt

<!--
![Build](https://github.com/GitHubPang/Rossynt/workflows/Build/badge.svg)
-->
[![Version](https://img.shields.io/jetbrains/plugin/v/16902.svg)](https://plugins.jetbrains.com/plugin/16902)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16902.svg)](https://plugins.jetbrains.com/plugin/16902)

<img src="docs/screenshot01.png" width="345" /> <img src="docs/screenshot02.png" width="345" />

<!-- Plugin description -->
Rossynt - **Ros**lyn **syn**tax **t**ree viewer for C#. Works on JetBrains Rider and all other IntelliJ IDEA-based IDEs.

## System Requirements

* [ASP.NET Core Runtime](https://dotnet.microsoft.com/download/dotnet) (2.1, 3.1, or 5.0)
* [.NET CLI tool](https://docs.microsoft.com/en-us/dotnet/core/tools/)

These are all included if you have installed [.NET SDK](https://dotnet.microsoft.com/download/dotnet) or [Visual Studio](https://visualstudio.microsoft.com/downloads/).

## Features

* View syntax tree of C# files, including scratch files.
* View properties of each node.
* Highlight selected node in source code.
* Find corresponding node for selected text.
* Support light theme and dark theme.

## Settings

* To configure the plugin, go to
    * `Settings | Tools | Rossynt` for Windows and Linux
    * `Preferences | Tools | Rossynt` for macOS

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Rossynt"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/GitHubPang/Rossynt/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
<sub>Plugin based on the [IntelliJ Platform Plugin Template][template].</sub>

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
