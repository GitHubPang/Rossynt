package org.example.githubpang.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.lang3.StringUtils
import org.example.githubpang.BackendRuntimeVersion
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class BackendService : Disposable {
    private companion object {
        const val DEPLOY_DIRECTORY_PREFIX = "RoslynSyntaxTreeBackend."
        const val RESOURCE_BACKEND_PATH = "/raw/RoslynSyntaxTreeBackend"
        const val RESOURCE_FILE_LIST_FILE_NAME = "FileList.txt"
        const val BACKEND_DLL_NAME = "RoslynSyntaxTreeBackend.dll"

        /**
         * [Reference](https://docs.microsoft.com/en-us/dotnet/core/install/how-to-detect-installed-versions#check-for-install-folders)
         */
        val DEFAULT_DOT_NET_PATHS = arrayOf(
            "C:\\program files\\dotnet\\dotnet.exe", // Default location of dotnet executable on Windows.
            "C:\\Users\\User\\.dotnet\\dotnet.exe", // Default location of dotnet executable on Windows as installed by Rider.
            "/home/user/share/dotnet/dotnet", // Default location of dotnet executable on Linux.
            "/usr/local/share/dotnet/dotnet", // Default location of dotnet executable on macOS.
        )
    }

    // ******************************************************************************** //

    private var dotNetPath: String = ""
    private var backendRuntimeVersion: BackendRuntimeVersion = BackendRuntimeVersion.DOT_NET_5
    private var deployPath: Path? = null
    private var backendProcess: Process? = null

    // ******************************************************************************** //

    init {
        initBackend()
    }

    override fun dispose() {
        shutdownBackend()
    }

    private fun initBackend() {
        //todo do in background thread. Co-routine?
        //todo try catch?

        // Get basic info.
        dotNetPath = findDotNetPath()
        backendRuntimeVersion = getBackendRuntimeVersion()

        // Create deploy directory.
        deployPath = Files.createTempDirectory(DEPLOY_DIRECTORY_PREFIX)

        // Deploy files.
        deployFiles()

        // Execute backend.
        backendProcess = executeBackend()
    }

    private fun shutdownBackend() {
        try {
            // Kill backend process.
            backendProcess?.destroy()
            backendProcess?.waitFor(500, TimeUnit.MILLISECONDS)
            backendProcess?.destroyForcibly()
            backendProcess = null

            // Delete deploy directory.
            deployPath?.toFile()?.deleteRecursively()
            deployPath = null
            //todo some files left behind. Why? Files still held by OS?
        } catch (e: Exception) {
            Logger.getInstance(javaClass).info(e)
        }
    }

    private fun findDotNetPath(): String {
        // todo should allow config dotnet path in settings
        DEFAULT_DOT_NET_PATHS.forEach { dotNetPath ->
            if (!File(dotNetPath).exists()) {
                return@forEach
            }

            return dotNetPath
        }

        throw IllegalStateException()//todo
    }

    private fun getBackendRuntimeVersion(): BackendRuntimeVersion {
        val pattern = Pattern.compile("^Microsoft.AspNetCore.App ([^ ]+) .*$")
        val processOutput = ExecUtil.execAndGetOutput(GeneralCommandLine(dotNetPath, "--list-runtimes"))

        var highestBackendRuntimeVersion: BackendRuntimeVersion? = null
        processOutput.stdoutLines.forEach { stdoutLine ->
            val matcher = pattern.matcher(stdoutLine)
            if (!matcher.matches()) {
                return@forEach
            }

            val version = matcher.group(1)
            val majorVersion = StringUtils.split(version, '.')[0].toInt()
            val backendRuntimeVersion = BackendRuntimeVersion.values().find { majorVersion == it.majorVersion }
                ?: return@forEach

            if (highestBackendRuntimeVersion == null || highestBackendRuntimeVersion!!.majorVersion < backendRuntimeVersion.majorVersion) {
                highestBackendRuntimeVersion = backendRuntimeVersion
            }
        }

        if (highestBackendRuntimeVersion == null) {
            throw IllegalStateException()//todo
        }

        return highestBackendRuntimeVersion as BackendRuntimeVersion
    }

    private fun deployFiles() {
        val deployPath = deployPath ?: throw IllegalStateException()

        val fileListFile = "$RESOURCE_BACKEND_PATH/${backendRuntimeVersion.directoryName}/$RESOURCE_FILE_LIST_FILE_NAME"
        javaClass.getResourceAsStream(fileListFile).bufferedReader().useLines { lines ->
            for (line in lines) {
                val inFile = "$RESOURCE_BACKEND_PATH/${backendRuntimeVersion.directoryName}/$line"
                val outFile = File(deployPath.toFile(), line)

                javaClass.getResourceAsStream(inFile).use { inputStream ->
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private fun executeBackend(): Process {
        val deployPath = deployPath ?: throw IllegalStateException()
        val dllFullPath = File(deployPath.toFile(), BACKEND_DLL_NAME).absolutePath
        return GeneralCommandLine(dotNetPath, dllFullPath).createProcess()
    }
}
