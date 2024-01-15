package org.example.githubpang.rossynt.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfoRt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.example.githubpang.rossynt.settings.PluginSettingsData
import org.example.githubpang.rossynt.trees.TreeNode
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.EmptyCoroutineContext

internal class BackendService : IBackendService {
    private companion object {
        private val LOGGER = Logger.getInstance(BackendService::class.java)

        private const val DEPLOY_DIRECTORY_PREFIX = "RossyntBackend."
        private const val RESOURCE_BACKEND_PATH = "/raw/RossyntBackend"
        private const val RESOURCE_FILE_LIST_FILE_NAME = "FileList.txt"
        private const val BACKEND_DLL_NAME = "RossyntBackend.dll"
        private const val PING_BACKEND_DELAY_DURATION_MILLISECONDS = (1000 * 60 / 3.5).toLong()
        private const val RETRY_SEND_REQUEST_DURATION_MILLISECONDS = 1000L
        private const val DELETE_DEPLOY_PATH_MAX_RETRY_COUNT = 20
        private const val DELETE_DEPLOY_PATH_DELAY_DURATION_MILLISECONDS = 75L

        /**
         * [Reference](https://docs.microsoft.com/en-us/dotnet/core/install/how-to-detect-installed-versions#check-for-install-folders)
         */
        private val DEFAULT_DOT_NET_PATHS = if (SystemInfoRt.isWindows) {
            arrayOf(
                "C:\\program files\\dotnet\\dotnet.exe", // Default location of dotnet executable on Windows.
                File(System.getProperty("user.home"), ".dotnet\\dotnet.exe").absolutePath, // Default location of dotnet executable on Windows as installed by Rider.
            )
        } else {
            arrayOf(
                "/home/user/share/dotnet/dotnet", // Default location of dotnet executable on Linux.
                "/usr/bin/dotnet", // Default location of dotnet executable on Linux.
                "/usr/local/share/dotnet/dotnet", // Default location of dotnet executable on macOS.
                File(System.getProperty("user.home"), ".dotnet/dotnet").absolutePath, // Default location of dotnet executable on macOS as installed by Rider.
            )
        }
    }

    // ******************************************************************************** //

    private val scope = CoroutineScope(EmptyCoroutineContext + Job())

    @Volatile
    private var isReady = false
    private var isDisposed: AtomicBoolean = AtomicBoolean()
    private var project: Project? = null
    private var delegate: IBackendServiceDelegate? = null
    private var backendJob: Job? = null
    private var dotNetPath: String? = null
    private var backendRuntimeVersion: BackendRuntimeVersion? = null
    private var deployPath: Path? = null
    private var backendProcess: Process? = null
    private var backendUrl: String? = null
    private var backendExceptionMessage: String? = null

    // ******************************************************************************** //

    override fun startBackendService(project: Project, delegate: IBackendServiceDelegate?) {
        require(this.project == null)
        this.project = project
        this.delegate = delegate
        backendJob = scope.launch(Dispatchers.IO) {
            runBackend()
        }
    }

    override fun dispose() {
        if (!isDisposed.compareAndSet(false, true)) {
            return
        }

        try {
            LOGGER.info("Backend service dispose begin.")
            delegate = null
            runBlocking(Dispatchers.IO) {
                backendJob?.cancelAndJoin()
                backendJob = null

                shutdownBackend()
            }
            scope.cancel()
        } finally {
            LOGGER.info("Backend service dispose end.")
        }
    }

    private suspend fun runBackend() {
        val project = project ?: throw IllegalStateException()

        try {
            // Find dot net path.
            dotNetPath = findDotNetPath()
            LOGGER.info("Found dot net path: $dotNetPath")

            // Get backend runtime version.
            yield()
            val backendRuntimeVersionFinder = BackendRuntimeVersionFinder(dotNetPath ?: throw IllegalStateException())
            backendRuntimeVersion = backendRuntimeVersionFinder.backendRuntimeVersion
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
            backendProcess = executeBackend(project)
            isReady = true
            LOGGER.info("Started backend process, backendUrl = $backendUrl")

            // Publish message.
            scope.launch(Dispatchers.Main) {
                val messageBus = project.messageBus
                val publisher = messageBus.syncPublisher(BackendServiceNotifier.TOPIC)
                publisher.backendServiceBecameReady()
            }

            // Loop until cancelled...
            while (true) {
                delay(PING_BACKEND_DELAY_DURATION_MILLISECONDS)
                pingBackend()   // Ping backend to keep it alive.
            }
        } catch (e: Exception) {
            // Skip if already disposed.
            if (isDisposed.get()) {
                return
            }

            // Skip if cancelled.
            if (e is CancellationException) {
                return
            }

            // Handle backend exception.
            if (e is BackendException) {
                backendExceptionMessage = e.localizedMessage
                if (e.cause != null) {
                    backendExceptionMessage += " - " + e.cause?.localizedMessage
                }

                // Inform delegate.
                scope.launch(Dispatchers.Main) {
                    this@BackendService.delegate?.onBackendExceptionMessageUpdated(backendExceptionMessage)
                }

                return
            }

            // Write log.
            LOGGER.error(e)
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
            if (!isDisposed.get()) {
                LOGGER.warn(e)
            }
        }
    }

    private fun findDotNetPath(): String {
        return PluginSettingsData.instance.dotNetPath ?: autoFindDotNetPath()
    }

    private fun autoFindDotNetPath(): String {
        DEFAULT_DOT_NET_PATHS.forEach { dotNetPath ->
            if (!File(dotNetPath).exists()) {
                return@forEach
            }

            return dotNetPath
        }

        throw BackendException("Unable to find .NET CLI tool.")
    }

    private fun deployFiles() {
        val backendRuntimeVersion = backendRuntimeVersion ?: throw IllegalStateException()
        val deployPath = deployPath ?: throw IllegalStateException()

        val fileListFile = "$RESOURCE_BACKEND_PATH/${backendRuntimeVersion.directoryName}/$RESOURCE_FILE_LIST_FILE_NAME"
        val fileListStream = javaClass.getResourceAsStream(fileListFile) ?: throw Exception("Error loading file list file.")
        fileListStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                val inFile = "$RESOURCE_BACKEND_PATH/${backendRuntimeVersion.directoryName}/$line"
                val outFile = File(deployPath.toFile(), line)

                javaClass.getResourceAsStream(inFile).use { inputStream ->
                    if (inputStream == null) throw Exception("Error loading file: $inFile")

                    Files.createDirectories(outFile.parentFile.toPath())
                    FileOutputStream(outFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private suspend fun executeBackend(parentDisposable: Disposable): Process {
        val dotNetPath = dotNetPath ?: throw IllegalStateException()
        val deployPath = deployPath ?: throw IllegalStateException()

        // Construct command line.
        val dllFullPath = File(deployPath.toFile(), BACKEND_DLL_NAME).absolutePath
        val workingDirectory = File(dotNetPath).parent
        @Suppress("HttpUrlsUsage") val commandLine = GeneralCommandLine(dotNetPath, dllFullPath, "--urls", "http://*:0").withWorkDirectory(workingDirectory)

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
                    val parseResult = BackendProcessOutputParser.parseText(event.text)
                    if (parseResult != null) {
                        // Send backend url to channel.
                        scope.launch(Dispatchers.IO) {
                            backendUrlChannel.send(parseResult.backendUrl)
                        }
                    }
                }
            }
        }
        osProcessHandler.addProcessListener(processListener, parentDisposable)
        osProcessHandler.startNotify()

        // Wait until got backend URL from channel.
        backendUrl = backendUrlChannel.receive()

        // Stop listening.
        osProcessHandler.removeProcessListener(processListener)
        return process
    }

    override suspend fun compileFile(fileText: String?, filePath: String?, cSharpVersion: CSharpVersion): TreeNode? {
        return if (fileText != null && filePath != null && RossyntUtil.isCSFile(filePath)) {
            sendRequestToBackend("syntaxTree/compileFile", parametersOf("FileText", fileText).plus(parametersOf("FilePath", filePath)).plus(parametersOf("CSharpVersion", cSharpVersion.name)))
        } else {
            sendRequestToBackend<String>("syntaxTree/resetActiveFile")
            null
        }
    }

    override suspend fun getNodeInfo(nodeId: String): Map<String, String> {
        return sendRequestToBackend("syntaxTree/getNodeInfo", parametersOf("NodeId", nodeId)) ?: HashMap()
    }

    override suspend fun findNode(start: Int, end: Int): String? {
        val response: Map<String, String> = sendRequestToBackend("syntaxTree/findNode", parametersOf("Start", start.toString()).plus(parametersOf("End", end.toString()))) ?: HashMap()
        return response["nodeId"]
    }

    private suspend fun pingBackend() {
        sendRequestToBackend<Unit>("syntaxTree/ping")
    }

    private suspend inline fun <reified T> sendRequestToBackend(urlPath: String, formParameters: Parameters = Parameters.Empty): T? {
        try {
            while (true) {
                // Skip if already disposed.
                if (isDisposed.get()) {
                    return null
                }

                // Retry later if not ready yet.
                if (!isReady) {
                    delay(RETRY_SEND_REQUEST_DURATION_MILLISECONDS)
                    continue
                }

                break
            }

            // Do HTTP request.
            HttpClient(CIO) {
                install(ContentNegotiation) { gson() }
            }.use { client ->
                return client.submitForm("$backendUrl/$urlPath", formParameters).body()
            }
        } catch (e: Exception) {
            if (!isDisposed.get()) {
                LOGGER.warn(e)
            }
            return null
        }
    }
}
