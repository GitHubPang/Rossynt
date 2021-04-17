package org.example.githubpang.rossynt.services

import com.google.common.collect.ImmutableMap
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.listeners.ProjectToolWindowManagerListener
import org.example.githubpang.rossynt.trees.TreeNode
import java.util.*
import javax.annotation.concurrent.Immutable

@Service
internal class RossyntService : Disposable {
    @Immutable
    private class State(val fileText: String?, val filePath: String?, val nodeId: String?) {
        val uniqueId: UUID = UUID.randomUUID()

        constructor() : this(null, null, null)
    }

    @Immutable
    private class Data(val fileText: String?, val filePath: String?, val rootTreeNode: TreeNode?, val nodeId: String?, nodeInfo: ImmutableMap<String, String>?) {
        val nodeInfo: ImmutableMap<String, String> = nodeInfo ?: ImmutableMap.of()

        constructor() : this(null, null, null, null, null)
    }

    // ******************************************************************************** //

    private var project: Project? = null

    private var toolWindowIsVisible = false
    private var backendService: BackendService? = null
    private var isBackendServiceStarted = false

    private var expectedState: State = State()
    private var isRefreshingCurrentData = false
    private var currentData: Data = Data()

    // ******************************************************************************** //

    override fun dispose() = Unit

    fun startRossyntService(project: Project) {
        require(this.project == null)
        this.project = project

        val messageBusConnection = project.messageBus.connect()
        messageBusConnection.subscribe(ToolWindowManagerListener.TOPIC, ProjectToolWindowManagerListener(project))
        messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                super.selectionChanged(event)

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
                expectedState = State(fileText, filePath, null)

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
                    backendService = project.service()
                    backendService?.startBackendService(project)
                }
            }
        })
        messageBusConnection.subscribe(BackendServiceNotifier.TOPIC, object : BackendServiceNotifier {
            override fun backendServiceBecameReady() {
                isBackendServiceStarted = true

                // Refresh current data.
                refreshCurrentData()
            }
        })

        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)

                if (FileEditorManager.getInstance(project).selectedTextEditor?.document != event.document) {
                    return
                }

                // Update expected state.
                val fileText = event.document.text
                expectedState = State(fileText, expectedState.filePath, null)

                // Refresh current data.
                refreshCurrentData()
            }
        }, this)
    }

    fun setCurrentNodeId(nodeId: String?) {
        // Update expected state.
        expectedState = State(expectedState.fileText, expectedState.filePath, nodeId)

        // Refresh current data.
        refreshCurrentData()
    }

    private fun refreshCurrentData() {
        val backendService = backendService ?: return

        if (!isBackendServiceStarted) {
            return
        }

        if (isRefreshingCurrentData) {
            return
        }
        isRefreshingCurrentData = true

        //todo should check file extension?

        // If file text or file path outdated, update tree.
        if (currentData.fileText != expectedState.fileText || currentData.filePath != expectedState.filePath) {
            setCurrentData(Data())

            val fetchingState = expectedState
            GlobalScope.launch(Dispatchers.IO) {
                val rootTreeNode = backendService.compileFile(fetchingState.fileText, fetchingState.filePath)
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(fetchingState.fileText, fetchingState.filePath, rootTreeNode, null, null))
                    }

                    isRefreshingCurrentData = false
                    refreshCurrentData()
                }
            }

            return
        }

        // If node id outdated, update node info.
        if (currentData.nodeId != expectedState.nodeId) {
            setCurrentData(Data(currentData.fileText, currentData.filePath, currentData.rootTreeNode, null, null))

            val fetchingState = expectedState
            GlobalScope.launch(Dispatchers.IO) {
                val nodeInfo = when {
                    fetchingState.nodeId != null -> ImmutableMap.copyOf(backendService.getNodeInfo(fetchingState.nodeId))
                    else -> ImmutableMap.of()
                }
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(currentData.fileText, currentData.filePath, currentData.rootTreeNode, fetchingState.nodeId, nodeInfo))
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
