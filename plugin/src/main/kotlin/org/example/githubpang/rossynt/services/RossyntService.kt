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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.trees.TreeNode

@Service
internal class RossyntService {
    private var project: Project? = null
    private var currentFilePath: String? = null
    private var currentNodeId: String? = null
    private var toolWindowIsVisible = false
    private var backendService: BackendService? = null
    private var rootTreeNode: TreeNode? = null
    private var currentNodeInfo: ImmutableMap<String, String> = ImmutableMap.of()

    // ******************************************************************************** //

    fun startRossyntService(project: Project) {
        require(this.project == null)
        this.project = project

        val messageBusConnection = project.messageBus.connect()
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
                if (currentFilePath == filePath) {
                    return
                }

                // Set current data.
                currentFilePath = filePath
                currentNodeId = null

                // Update data.
                updateTree()
                updateCurrentNodeInfo()
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

                    // Update data.
                    updateTree()
                    updateCurrentNodeInfo()
                }
            }
        })
        messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                super.after(events)

                if (events.none { event -> event.path == currentFilePath }) {
                    return
                }

                currentNodeId = null

                // Update data.
                updateTree()
                updateCurrentNodeInfo()
            }
        })
        messageBusConnection.subscribe(BackendServiceNotifier.TOPIC, object : BackendServiceNotifier {
            override fun backendServiceBecameReady() {
                // Update data.
                updateTree()
            }
        })
    }

    fun setCurrentNodeId(nodeId: String?) {
        currentNodeId = nodeId

        // Update data.
        updateCurrentNodeInfo()
    }

    private fun updateTree() {
        //todo should check file extension?
        val project = project ?: throw IllegalStateException()
        val backendService = backendService ?: return//todo return here is correct?
        if (backendService.isReady) {
            //todo should do something if current request in progress
            GlobalScope.launch(Dispatchers.IO) {
                val result = backendService.setActiveFile(currentFilePath)
                launch(Dispatchers.Main) {
                    rootTreeNode = result

                    // Publish message.
                    val messageBus = project.messageBus
                    val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
                    publisher.treeUpdated(rootTreeNode)
                }
            }
        } else {
            //todo what to do here?
        }
    }

    private fun updateCurrentNodeInfo() {
        //todo should check file extension?
        val project = project ?: throw IllegalStateException()
        val backendService = backendService ?: return//todo return here is correct?
        val currentNodeId = currentNodeId
        if (currentNodeId != null) {
            if (backendService.isReady) {
                //todo should do something if current request in progress
                GlobalScope.launch(Dispatchers.IO) {
                    val result = ImmutableMap.copyOf(backendService.getNodeInfo(currentNodeId))
                    launch(Dispatchers.Main) {
                        currentNodeInfo = result

                        // Publish message.
                        val messageBus = project.messageBus
                        val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
                        publisher.currentNodeInfoUpdated(currentNodeInfo)
                    }
                }
            } else {
                //todo what to do here?
            }
        } else {
            currentNodeInfo = ImmutableMap.of()
        }
    }
}
