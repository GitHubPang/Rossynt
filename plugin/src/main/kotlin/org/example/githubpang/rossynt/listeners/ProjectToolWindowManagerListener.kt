package org.example.githubpang.rossynt.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.services.RossyntService
import org.example.githubpang.rossynt.toolWindow.RossyntToolWindow

internal class ProjectToolWindowManagerListener(private val project: Project) : ToolWindowManagerListener {
    private var rossyntToolWindowIsVisible: Boolean = false

    // ******************************************************************************** //

    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        super.stateChanged(toolWindowManager)

        val toolWindow = toolWindowManager.getToolWindow(RossyntToolWindow.TOOL_WINDOW_ID)
        if (toolWindow != null) {
            processRossyntToolWindowIsVisible(toolWindow)
        }
    }

    override fun toolWindowShown(toolWindow: ToolWindow) {
        super.toolWindowShown(toolWindow)

        if (toolWindow.id == RossyntToolWindow.TOOL_WINDOW_ID) {
            processRossyntToolWindowIsVisible(toolWindow)
        }
    }

    private fun processRossyntToolWindowIsVisible(toolWindow: ToolWindow) {
        if (rossyntToolWindowIsVisible == toolWindow.isVisible) {
            return
        }
        rossyntToolWindowIsVisible = toolWindow.isVisible

        // Start Rossynt service if needed.
        if (rossyntToolWindowIsVisible) {
            val rossyntService = project.service<RossyntService>()
            rossyntService.startRossyntServiceIfNeeded(project)
        }

        // Publish message.
        val messageBus = project.messageBus
        val publisher = messageBus.syncPublisher(RossyntToolWindowStateNotifier.TOPIC)
        publisher.rossyntToolWindowIsVisibleUpdated(rossyntToolWindowIsVisible)
    }
}
