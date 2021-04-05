package org.example.githubpang.rossynt.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import org.example.githubpang.rossynt.RossyntToolWindowStateNotifier
import org.example.githubpang.rossynt.toolWindow.RossyntToolWindow

internal class AppToolWindowManagerListener(private var project: Project) : ToolWindowManagerListener {
    private var rossyntToolWindowIsVisible: Boolean = false

    // ******************************************************************************** //

    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        super.stateChanged(toolWindowManager)

        val toolWindow = toolWindowManager.getToolWindow(RossyntToolWindow.TOOL_WINDOW_ID)
        if (toolWindow != null) {
            processRossyntToolWindowIsVisible(toolWindow.isVisible)
        }
    }

    override fun toolWindowShown(id: String, toolWindow: ToolWindow) {
        super.toolWindowShown(id, toolWindow)

        if (id == RossyntToolWindow.TOOL_WINDOW_ID) {
            processRossyntToolWindowIsVisible(true)
        }
    }

    private fun processRossyntToolWindowIsVisible(isVisible: Boolean) {
        if (rossyntToolWindowIsVisible == isVisible) {
            return
        }

        rossyntToolWindowIsVisible = isVisible

        // Publish message.
        val messageBus = project.messageBus
        val publisher = messageBus.syncPublisher(RossyntToolWindowStateNotifier.TOPIC)
        publisher.rossyntToolWindowIsVisibleUpdated(rossyntToolWindowIsVisible)
    }
}
