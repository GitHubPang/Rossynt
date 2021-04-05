package org.example.githubpang.rossynt.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.commons.lang3.StringUtils
import org.example.githubpang.rossynt.BackendRuntimeVersion
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Service
internal class BackendService : Disposable {
    private companion object {
        private val LOGGER = Logger.getInstance(BackendService::class.java)

        private const val DEPLOY_DIRECTORY_PREFIX = "RossyntBackend."
        private const val RESOURCE_BACKEND_PATH = "/raw/RossyntBackend"
        private const val RESOURCE_FILE_LIST_FILE_NAME = "FileList.txt"
        private const val BACKEND_DLL_NAME = "RossyntBackend.dll"
        private const val PING_BACKEND_DELAY_DURATION_MILLISECONDS = (1000 * 60 / 3.5).toLong()
        private const val DELETE_DEPLOY_PATH_MAX_RETRY_COUNT = 20
        private const val DELETE_DEPLOY_PATH_DELAY_DURATION_MILLISECONDS = 75L

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

    private var backendJob: Job
    private var dotNetPath: String? = null
    private var backendRuntimeVersion: BackendRuntimeVersion? = null
    private var deployPath: Path? = null
    private var backendProcess: Process? = null
    private var backendUrl: String? = null

    // ******************************************************************************** //

    init {
        backendJob = GlobalScope.launch(Dispatchers.IO) {
            runBackend()
        }
    }

    override fun dispose() {
        try {
            LOGGER.info("Backend service dispose begin.")
            runBlocking(Dispatchers.IO) {
                backendJob.cancelAndJoin()
                shutdownBackend()
            }
        } finally {
            LOGGER.info("Backend service dispose end.")
        }
    }

    private suspend fun runBackend() {
        try {
            // Find dot net path.
            dotNetPath = findDotNetPath()
            LOGGER.info("Found dot net path: $dotNetPath")

            // Get backend runtime version.
            yield()
            backendRuntimeVersion = getBackendRuntimeVersion()
            LOGGER.info("Backend runtime version: $backendRuntimeVersion")

            // Create deploy directory.
            yield()
            deployPath = withContext(Dispatchers.IO) { Files.createTempDirectory(DEPLOY_DIRECTORY_PREFIX) }
            LOGGER.info("Created deploy path: $deployPath")

            // Deploy files.
            yield()
            deployFiles()
            LOGGER.info("Deployed files to path: $deployPath")

            // Execute backend.
            yield()
            backendProcess = executeBackend()
            LOGGER.info("Started backend process, backendUrl = $backendUrl")

            // Loop until cancelled...
            while (true) {
                delay(PING_BACKEND_DELAY_DURATION_MILLISECONDS)
                pingBackend()   // Ping backend to keep it alive.
            }

        } catch (e: Exception) {
            if (e !is CancellationException) {
                LOGGER.error(e)
            }
        }
    }

    private suspend fun shutdownBackend() {
        try {
            // Kill backend process.
            LOGGER.info("Stopping Backend process...")
            backendProcess?.destroy()
            withContext(Dispatchers.IO) { backendProcess?.waitFor(500, TimeUnit.MILLISECONDS) }
            backendProcess?.destroyForcibly()
            backendProcess = null
            LOGGER.info("Backend process stopped.")

            // Delete deploy directory.
            for (loopIndex in 1..DELETE_DEPLOY_PATH_MAX_RETRY_COUNT) {
                LOGGER.info("Attempting to delete directory '$deployPath'...")
                deployPath?.toFile()?.deleteRecursively()

                if (deployPath?.toFile()?.exists() != true) {
                    LOGGER.info("Directory '$deployPath' successfully deleted.")
                    break
                }

                delay(DELETE_DEPLOY_PATH_DELAY_DURATION_MILLISECONDS)
            }
            deployPath = null
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
                    Files.createDirectories(outFile.parentFile.toPath())
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private suspend fun executeBackend(): Process {
        val dotNetPath = dotNetPath ?: throw IllegalStateException()
        val deployPath = deployPath ?: throw IllegalStateException()

        // Construct command line.
        val dllFullPath = File(deployPath.toFile(), BACKEND_DLL_NAME).absolutePath
        val workingDirectory = File(dotNetPath).parent
        val commandLine = GeneralCommandLine(dotNetPath, dllFullPath, "--urls", "http://*:0").withWorkDirectory(workingDirectory)

        // Create process.
        val process = commandLine.createProcess()
        val osProcessHandler = OSProcessHandler(process, commandLine.preparedCommandLine)

        // Create channel.
        val backendUrlChannel = Channel<String>()

        // Listen to process stdout.
        val processListener = object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                super.onTextAvailable(event, outputType)

                if (ProcessOutputType.isStdout(outputType)) {
                    val pattern = Pattern.compile("(http[s]?)://.+?:([0-9]+)")
                    val matcher = pattern.matcher(event.text)
                    if (matcher.find()) {
                        val urlScheme = matcher.group(1)
                        val serverPort = matcher.group(2)
                        val backendUrl = "$urlScheme://localhost:$serverPort"

                        // Send backend url to channel.
                        GlobalScope.launch(Dispatchers.IO) {
                            backendUrlChannel.send(backendUrl)
                        }
                    }
                }
            }
        }
        osProcessHandler.addProcessListener(processListener, this)
        osProcessHandler.startNotify()

        // Wait until got backend URL from channel.
        backendUrl = backendUrlChannel.receive()

        // Stop listening.
        osProcessHandler.removeProcessListener(processListener)
        return process
    }

    private suspend fun pingBackend() {
        HttpClient(CIO).use { client ->
            client.post<Unit>("$backendUrl/syntaxTree/ping")
        }//todo verify connection actually closed
    }
}
