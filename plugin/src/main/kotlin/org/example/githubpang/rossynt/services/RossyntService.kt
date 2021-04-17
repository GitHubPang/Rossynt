package org.example.githubpang.rossynt.services

import com.google.common.collect.ImmutableMap
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
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
internal class RossyntService {
    @Immutable
    private class State(val filePath: String? = null, val nodeId: String? = null) {
        val uniqueId: UUID = UUID.randomUUID()
    }

    @Immutable
    private class Data(val filePath: String? = null, val rootTreeNode: TreeNode? = null, val nodeId: String? = null, val nodeInfo: ImmutableMap<String, String> = ImmutableMap.of())

    // ******************************************************************************** //

    private var project: Project? = null

    private var toolWindowIsVisible = false
    private var backendService: BackendService? = null
    private var isBackendServiceStarted = false

    private var expectedState: State = State()
    private var isRefreshingCurrentData = false
    private var currentData: Data = Data()

    // ******************************************************************************** //

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
                expectedState = State(filePath)

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
        messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                super.after(events)

                if (events.none { event -> event.path == expectedState.filePath }) {
                    return
                }

                // Update expected state.
                expectedState = State(expectedState.filePath)

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
    }

    fun setCurrentNodeId(nodeId: String?) {
        // Update expected state.
        expectedState = State(expectedState.filePath, nodeId)

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

        // If file path outdated, update tree.
        if (currentData.filePath != expectedState.filePath) {
            setCurrentData(Data())

            val fetchingState = expectedState
            GlobalScope.launch(Dispatchers.IO) {
                val rootTreeNode = backendService.setActiveFile(fetchingState.filePath)
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(fetchingState.filePath, rootTreeNode))
                    }

                    isRefreshingCurrentData = false
                    refreshCurrentData()
                }
            }

            return
        }

        // If node id outdated, update node info.
        if (currentData.nodeId != expectedState.nodeId) {
            setCurrentData(Data(currentData.filePath, currentData.rootTreeNode))

            val fetchingState = expectedState
            GlobalScope.launch(Dispatchers.IO) {
                val nodeInfo = when {
                    fetchingState.nodeId != null -> ImmutableMap.copyOf(backendService.getNodeInfo(fetchingState.nodeId))
                    else -> ImmutableMap.of()
                }
                launch(Dispatchers.Main) {
                    if (fetchingState.uniqueId == expectedState.uniqueId) {
                        setCurrentData(Data(currentData.filePath, currentData.rootTreeNode, fetchingState.nodeId, nodeInfo))
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
