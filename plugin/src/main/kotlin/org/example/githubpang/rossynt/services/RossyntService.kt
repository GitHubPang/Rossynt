package org.example.githubpang.rossynt.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.example.githubpang.rossynt.CurrentFileNotifier
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier

@Service
class RossyntService {
    private var project: Project? = null
    private var currentFilePath: String? = null
    private var toolWindowIsVisible = false
    private var backendService: BackendService? = null

    // ******************************************************************************** //

    fun setupService(project: Project) {
        this.project = project

        val messageBusConnection = project.messageBus.connect()
        messageBusConnection.subscribe(CurrentFileNotifier.TOPIC, object : CurrentFileNotifier {
            override fun currentFileChanged(filePath: String?) {
                if (currentFilePath == filePath) {
                    return
                }

                currentFilePath = filePath
                startBackendServiceIfNeeded()
            }
        })
        messageBusConnection.subscribe(RossyntToolWindowStateNotifier.TOPIC, object : RossyntToolWindowStateNotifier {
            override fun rossyntToolWindowIsVisibleUpdated(isVisible: Boolean) {
                if (toolWindowIsVisible == isVisible) {
                    return
                }

                toolWindowIsVisible = isVisible
                startBackendServiceIfNeeded()
            }
        })
    }

    private fun startBackendServiceIfNeeded() {
        if (backendService == null && toolWindowIsVisible) {
            // Start backend service.
            backendService = project?.service()
        }
    }
}
