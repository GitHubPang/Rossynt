package org.example.githubpang.rossynt.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import org.apache.commons.lang3.StringUtils
import org.example.githubpang.rossynt.BackendRuntimeVersion
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class BackendService : Disposable {
    private companion object {
        private val LOGGER = Logger.getInstance(BackendService::class.java)

        private const val DEPLOY_DIRECTORY_PREFIX = "RossyntBackend."
        private const val RESOURCE_BACKEND_PATH = "/raw/RossyntBackend"
        private const val RESOURCE_FILE_LIST_FILE_NAME = "FileList.txt"
        private const val BACKEND_DLL_NAME = "RossyntBackend.dll"

        /**
         * [Reference](https://docs.microsoft.com/en-us/dotnet/core/install/how-to-detect-installed-versions#check-for-install-folders)
         */
        private val DEFAULT_DOT_NET_PATHS = arrayOf(
            "C:\\program files\\dotnet\\dotnet.exe", // Default location of dotnet executable on Windows.
            "C:\\Users\\User\\.dotnet\\dotnet.exe", // Default location of dotnet executable on Windows as installed by Rider.
            "/home/user/share/dotnet/dotnet", // Default location of dotnet executable on Linux.
            "/usr/local/share/dotnet/dotnet", // Default location of dotnet executable on macOS.
        )
    }

    // ******************************************************************************** //

    private var dotNetPath: String? = null
    private var backendRuntimeVersion: BackendRuntimeVersion? = null
    private var deployPath: Path? = null
    private var backendProcess: Process? = null
    private var backendUrl: String? = null

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
            //todo some files left behind. Why? Files still held by OS? Perhaps should retry
        } catch (e: Exception) {
            LOGGER.warn(e)
        }
    }

    private fun findDotNetPath(): String {
        // todo should allow config dotnet path in settings
        // todo: import com.intellij.openapi.util.SystemInfoRt; SystemInfoRt.isWindows
        DEFAULT_DOT_NET_PATHS.forEach { dotNetPath ->
            if (!File(dotNetPath).exists()) {
                return@forEach
            }

            return dotNetPath
        }

        throw IllegalStateException()//todo
    }

    private fun getBackendRuntimeVersion(): BackendRuntimeVersion {
        val dotNetPath = dotNetPath ?: throw IllegalStateException()
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
        val backendRuntimeVersion = backendRuntimeVersion ?: throw IllegalStateException()
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
        val dotNetPath = dotNetPath ?: throw IllegalStateException()
        val deployPath = deployPath ?: throw IllegalStateException()
        val dllFullPath = File(deployPath.toFile(), BACKEND_DLL_NAME).absolutePath
        val workingDirectory = File(dotNetPath).parent
        val commandLine = GeneralCommandLine(dotNetPath, dllFullPath, "--urls", "http://*:0").withWorkDirectory(workingDirectory)
        val process = commandLine.createProcess()
        val osProcessHandler = OSProcessHandler(process, commandLine.preparedCommandLine)
        osProcessHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                super.onTextAvailable(event, outputType)

                if (ProcessOutputType.isStdout(outputType)) {
                    val pattern = Pattern.compile("(http[s]?)://.+?:([0-9]+)")
                    val matcher = pattern.matcher(event.text)
                    if (matcher.find()) {
                        val urlScheme = matcher.group(1)
                        val serverPort = matcher.group(2)
                        backendUrl = "$urlScheme://localhost:$serverPort" // todo this is not the original UI thread here. Any problems?
                    }
                }
            }
        }, this)
        osProcessHandler.startNotify()
        return process
    }
}
