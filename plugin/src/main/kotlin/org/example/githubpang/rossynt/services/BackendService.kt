package org.example.githubpang.rossynt.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.example.githubpang.rossynt.trees.TreeNode
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

    var isReady = false
        private set
    private var project: Project? = null
    private var backendJob: Job? = null
    private var dotNetPath: String? = null
    private var backendRuntimeVersion: BackendRuntimeVersion? = null
    private var deployPath: Path? = null
    private var backendProcess: Process? = null
    private var backendUrl: String? = null

    // ******************************************************************************** //

    fun initService(project: Project) {
        this.project = project
        backendJob = GlobalScope.launch(Dispatchers.IO) {
            runBackend()
        }
    }

    override fun dispose() {
        try {
            LOGGER.info("Backend service dispose begin.")
            runBlocking(Dispatchers.IO) {
                backendJob?.cancelAndJoin()
                backendJob = null

                shutdownBackend()
            }
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
            backendProcess = executeBackend()
            isReady = true
            LOGGER.info("Started backend process, backendUrl = $backendUrl")

            // Publish message.
            val messageBus = project.messageBus
            val publisher = messageBus.syncPublisher(BackendServiceNotifier.TOPIC)
            publisher.backendServiceBecameReady()

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

    private fun deployFiles() {
        val backendRuntimeVersion = backendRuntimeVersion ?: throw IllegalStateException()
        val deployPath = deployPath ?: throw IllegalStateException()

        val fileListFile = "$RESOURCE_BACKEND_PATH/${backendRuntimeVersion.directoryName}/$RESOURCE_FILE_LIST_FILE_NAME"
        val fileListStream = javaClass.getResourceAsStream(fileListFile)
            ?: throw Exception("Error loading file list file.")
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

    suspend fun setActiveFile(filePath: String?): TreeNode? {
        return if (filePath != null) {
            sendRequestToBackend("syntaxTree/setActiveFile", parametersOf("FilePath", filePath))
        } else {
            sendRequestToBackend<String>("syntaxTree/resetActiveFile")
            null
        }
    }

    suspend fun getNodeInfo(nodeId: String): Map<String, String> {
        return sendRequestToBackend("syntaxTree/getNodeInfo", parametersOf("NodeId", nodeId))
    }

    private suspend fun pingBackend() {
        sendRequestToBackend<Unit>("syntaxTree/ping")
    }

    private suspend inline fun <reified T> sendRequestToBackend(urlPath: String, formParameters: Parameters = Parameters.Empty): T {
        //todo check isReady?
        HttpClient(CIO) {
            install(JsonFeature) { serializer = GsonSerializer() }
        }.use { client ->
            //todo what if this throws exception?
            return client.submitForm("$backendUrl/$urlPath", formParameters)
        }//todo verify connection actually closed
    }
}
