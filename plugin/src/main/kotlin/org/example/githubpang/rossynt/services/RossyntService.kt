package org.example.githubpang.rossynt.services

import com.google.common.collect.ImmutableMap
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.util.LineSeparator
import kotlinx.coroutines.*
import net.jcip.annotations.Immutable
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.events.ITextEventThrottlerCallback
import org.example.githubpang.rossynt.events.TextEventThrottler
import org.example.githubpang.rossynt.settings.PluginSettingsNotifier
import org.example.githubpang.rossynt.trees.TreeNode
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

@Service
internal class RossyntService : Disposable {
    @Immutable
    private class State(val fileText: String?, val lineSeparator: String, val filePath: String?, val cSharpVersion: CSharpVersion, val nodeId: String?) {
        val uniqueId: UUID = UUID.randomUUID()

        constructor() : this(null, "", null, CSharpVersion.Default, null)
    }

    @Immutable
    private class Data(val fileText: String?, val lineSeparator: String, val filePath: String?, val cSharpVersion: CSharpVersion, val rootTreeNode: TreeNode?, val nodeId: String?, nodeInfo: ImmutableMap<String, String>?) {
        val nodeInfo: ImmutableMap<String, String> = nodeInfo ?: ImmutableMap.of()

        constructor() : this(null, "", null, CSharpVersion.Default, null, null, null)
    }

    // ******************************************************************************** //

    private val scope = CoroutineScope(EmptyCoroutineContext + Job())

    private val textEventThrottler = TextEventThrottler()

    private var delegate: IRossyntService? = null
    private var project: Project? = null

    private var toolWindowIsVisible = false
    private var backendService: IBackendService? = null
    private var backendExceptionMessage: String? = null
    private var isBackendServiceStarted = false

    private var expectedState: State = State()
    private var isRefreshingCurrentData = false
    private var currentData: Data = Data()

    // ******************************************************************************** //

    init {
        Disposer.register(this, textEventThrottler)
    }

    override fun dispose() {
        scope.cancel()
        delegate = null
    }

    fun setDelegate(delegate: IRossyntService?) {
        this.delegate = delegate
    }

    fun getCurrentFilePath(): String? {
        return expectedState.filePath
    }

    fun setCSharpVersion(cSharpVersion: CSharpVersion) {
        // Update expected state.
        setExpectedState(State(expectedState.fileText, expectedState.lineSeparator, expectedState.filePath, cSharpVersion, null))

        // Refresh current data.
        refreshCurrentData()
    }

    fun getCSharpVersion(): CSharpVersion {
        return expectedState.cSharpVersion
    }

    fun startRossyntServiceIfNeeded(project: Project) {
        if (this.project != null) {
            return
        }
        this.project = project

        // Initialize expected state.
        initializeExpectedState(project)

        val messageBusConnection = project.messageBus.connect()
        messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                super.selectionChanged(event)

                // Reset text event throttler.
                textEventThrottler.reset()

                // Get event file path.
                val file = event.newFile
                val filePath = if (file != null && file.isValid && file.isInLocalFileSystem) {
                    file.path
                } else {
                    null
                }

                // Skip if no change.
                if (filePath == expectedState.filePath) {
                    return
                }

                // Update expected state.
                val fileText = FileEditorManager.getInstance(project).selectedTextEditor?.document?.text
                val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)
                setExpectedState(State(fileText, lineSeparator, filePath, expectedState.cSharpVersion, null))

                // Refresh current data.
                refreshCurrentData()
            }
        })
        messageBusConnection.subscribe(RossyntToolWindowStateNotifier.TOPIC, object : RossyntToolWindowStateNotifier {
            override fun rossyntToolWindowIsVisibleUpdated(isVisible: Boolean) {
                if (toolWindowIsVisible == isVisible) {
                    return
                }
                toolWindowIsVisible = isVisible

                // Start backend service if needed.
                if (backendService == null && toolWindowIsVisible) {
                    backendService = project.service<RestartableBackendService>()
                    backendService?.startBackendService(project, object : IBackendServiceDelegate {
                        override fun onBackendExceptionMessageUpdated(backendExceptionMessage: String?) {
                            if (this@RossyntService.backendExceptionMessage == backendExceptionMessage) {
                                return
                            }
                            this@RossyntService.backendExceptionMessage = backendExceptionMessage

                            // Publish message.
                            val messageBus = project.messageBus
                            val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
                            publisher.backendExceptionMessageUpdated(this@RossyntService.backendExceptionMessage)
                        }
                    })
                }

                // Refresh current data.
                refreshCurrentData()
            }
        })
        messageBusConnection.subscribe(BackendServiceNotifier.TOPIC, object : BackendServiceNotifier {
            override fun backendServiceBecameReady() {
                isBackendServiceStarted = true

                // Refresh current data.
                refreshCurrentData()
            }
        })
        project.messageBus.connect().subscribe(PluginSettingsNotifier.TOPIC, object : PluginSettingsNotifier {
            override fun pluginSettingsUpdated() {
                // Reset current data.
                setCurrentData(Data())

                // Refresh current data.
                refreshCurrentData()
            }
        })

        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)

                scope.launch(Dispatchers.Main) {
                    if (FileEditorManager.getInstance(project).selectedTextEditor?.document != event.document) {
                        return@launch
                    }

                    textEventThrottler.queueEvent(event.document.text)
                }
            }
        }, this)
        textEventThrottler.setCallback(object : ITextEventThrottlerCallback {
            override fun onTextEvent(text: String) {
                val file = FileEditorManager.getInstance(project).selectedEditor?.file
                val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)

                // Update expected state.
                setExpectedState(State(text, lineSeparator, expectedState.filePath, expectedState.cSharpVersion, null))

                // Refresh current data.
                refreshCurrentData()
            }
        })
    }

    private fun initializeExpectedState(project: Project) {
        val fileText = FileEditorManager.getInstance(project).selectedTextEditor?.document?.text
        val file = FileEditorManager.getInstance(project).selectedEditor?.file
        val filePath = file?.path
        val lineSeparator = FileDocumentManager.getInstance().getLineSeparator(file, project)
        setExpectedState(State(fileText, lineSeparator, filePath, CSharpVersion.Default, null))
    }

    fun setCurrentNodeId(nodeId: String?) {
        // Update expected state.
        setExpectedState(State(expectedState.fileText, expectedState.lineSeparator, expectedState.filePath, expectedState.cSharpVersion, nodeId))

        // Refresh current data.
        refreshCurrentData()
    }

    fun findNodeAtCaret() {
        val backendService = backendService ?: return

        if (!isBackendServiceStarted) {
            return
        }

        val selection = getSelection() ?: return
        scope.launch(Dispatchers.IO) {
            val nodeId = backendService.findNode(selection.startOffset, selection.endOffset)
            launch(Dispatchers.Main) innerLaunch@{
                val newSelection = getSelection() ?: return@innerLaunch
                if (selection == newSelection) {
                    delegate?.onFindNodeAtCaretResult(nodeId)
                }
            }
        }
    }

    private fun getSelection(): TextRange? {
        val project = project ?: return null
        val selection = FileEditorManager.getInstance(project).selectedTextEditor?.selectionModel ?: return null
        val selectionStart = convertToPhysicalOffset(selection.selectionStart) ?: return null
        val selectionEnd = convertToPhysicalOffset(selection.selectionEnd) ?: return null

        return TextRange(selectionStart, selectionEnd)
    }

    fun convertToVirtualOffset(physicalOffset: Int): Int? {
        val fileText = LineSeparatorUtil.convertLineSeparators(currentData.fileText, currentData.lineSeparator) ?: return null
        return LineSeparatorUtil.convertOffset(physicalOffset.coerceAtMost(fileText.length), fileText, currentData.lineSeparator, LineSeparator.LF.separatorString)
    }

    private fun convertToPhysicalOffset(virtualOffset: Int): Int? {
        val fileText = currentData.fileText ?: return null
        return LineSeparatorUtil.convertOffset(virtualOffset.coerceAtMost(fileText.length), fileText, LineSeparator.LF.separatorString, currentData.lineSeparator)
    }

    private fun refreshCurrentData() {
        val backendService = backendService ?: return

        if (!isBackendServiceStarted) {
            return
        }
        if (!toolWindowIsVisible) {
            return
        }

        if (isRefreshingCurrentData) {
            return
        }
        isRefreshingCurrentData = true

        // If file text or file path or C# version outdated, update tree.
        if (currentData.fileText != expectedState.fileText || currentData.lineSeparator != expectedState.lineSeparator || currentData.filePath != expectedState.filePath || currentData.cSharpVersion != expectedState.cSharpVersion) {
            setCurrentData(Data())

            val fetchingState = expectedState
            scope.launch(Dispatchers.IO) {
                val rootTreeNode = backendService.compileFile(LineSeparatorUtil.convertLineSeparators(fetchingState.fileText, fetchingState.lineSeparator), fetchingState.filePath, fetchingState.cSharpVersion)
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(fetchingState.fileText, fetchingState.lineSeparator, fetchingState.filePath, fetchingState.cSharpVersion, rootTreeNode, null, null))
                    }

                    isRefreshingCurrentData = false
                    refreshCurrentData()
                }
            }

            return
        }

        // If node id outdated, update node info.
        if (currentData.nodeId != expectedState.nodeId) {
            setCurrentData(Data(currentData.fileText, currentData.lineSeparator, currentData.filePath, currentData.cSharpVersion, currentData.rootTreeNode, null, null))

            val fetchingState = expectedState
            scope.launch(Dispatchers.IO) {
                val nodeInfo = when {
                    fetchingState.nodeId != null -> ImmutableMap.copyOf(backendService.getNodeInfo(fetchingState.nodeId))
                    else -> ImmutableMap.of()
                }
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(currentData.fileText, currentData.lineSeparator, currentData.filePath, currentData.cSharpVersion, currentData.rootTreeNode, fetchingState.nodeId, nodeInfo))
                    }

                    isRefreshingCurrentData = false
                    refreshCurrentData()
                }
            }

            return
        }

        // Everything is updated. Done.
        isRefreshingCurrentData = false
    }

    private fun setExpectedState(expectedState: State) {
        val oldFilePath = this.expectedState.filePath
        val newFilePath = expectedState.filePath
        this.expectedState = expectedState

        if (oldFilePath != newFilePath) {
            delegate?.onCurrentFilePathUpdated(newFilePath)
        }
    }

    private fun setCurrentData(newData: Data) {
        val project = project ?: throw IllegalStateException()

        // Update current data.
        val oldData = currentData
        currentData = newData

        // If tree has changed...
        if (currentData.rootTreeNode != oldData.rootTreeNode) {
            // Publish message.
            val messageBus = project.messageBus
            val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
            publisher.treeUpdated(currentData.rootTreeNode)
        }

        // If node info has changed...
        if (currentData.nodeInfo != oldData.nodeInfo) {
            // Publish message.
            val messageBus = project.messageBus
            val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
            publisher.nodeInfoUpdated(currentData.nodeInfo)
        }
    }
}
