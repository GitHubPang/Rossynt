package org.example.githubpang.rossynt.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.githubpang.rossynt.CurrentFileNotifier
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.TreeNode

@Service
internal class RossyntService {
    private var project: Project? = null
    private var currentFilePath: String? = null
    private var toolWindowIsVisible = false
    private var backendService: BackendService? = null
    private var rootTreeNode: TreeNode? = null

    // ******************************************************************************** //

    fun initService(project: Project) {
        require(this.project == null)
        this.project = project

        val messageBusConnection = project.messageBus.connect()
        messageBusConnection.subscribe(CurrentFileNotifier.TOPIC, object : CurrentFileNotifier {
            override fun currentFileChanged(filePath: String?) {
                if (currentFilePath == filePath) {
                    return
                }
                currentFilePath = filePath

                // Update tree.
                updateTree()
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
                    backendService?.initService(project)

                    // Update tree.
                    updateTree()
                }
            }
        })
        messageBusConnection.subscribe(BackendServiceNotifier.TOPIC, object : BackendServiceNotifier {
            override fun backendServiceBecameReady() {
                // Update tree.
                updateTree()
            }
        })
    }

    private fun updateTree() {
        val project = project ?: throw IllegalStateException()
        val backendService = backendService ?: return//todo return here is correct?
        if (backendService.isReady) {
            //todo should do something if current request in progress
            GlobalScope.launch(Dispatchers.IO) {
                rootTreeNode = backendService.setActiveFile(currentFilePath)

                // Publish message.
                val messageBus = project.messageBus
                val publisher = messageBus.syncPublisher(RossyntServiceNotifier.TOPIC)
                publisher.treeUpdated(rootTreeNode)
            }
        } else {
            //todo what to do here?
        }
    }
}
