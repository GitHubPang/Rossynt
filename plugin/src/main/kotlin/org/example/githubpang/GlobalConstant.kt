package org.example.githubpang

import java.nio.file.Files
import java.nio.file.Path

class GlobalConstant {
    companion object {
        private const val TEMP_DIRECTORY_PREFIX = "RoslynSyntaxTreeBackend."
        val TEMP_PATH: Path by lazy { Files.createTempDirectory(TEMP_DIRECTORY_PREFIX) }

        const val RESOURCE_BACKEND_PATH = "/raw/RoslynSyntaxTreeBackend/"

        @Suppress("SpellCheckingInspection")
        val RESOURCE_BACKEND_FILE_NAME_ARRAY: Array<String> = arrayOf(
            "appsettings.Development.json",
            "appsettings.json",
            "RoslynSyntaxTreeBackend.deps.json",
            "RoslynSyntaxTreeBackend.dll",
            "RoslynSyntaxTreeBackend.exe",
            "RoslynSyntaxTreeBackend.pdb",
            "RoslynSyntaxTreeBackend.runtimeconfig.json",
            "web.config",
        )
    }
}
