package org.example.githubpang.rossynt.listeners

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import org.example.githubpang.rossynt.toolWindow.RossyntToolWindow

internal class MyToolWindowManagerListener : ToolWindowManagerListener {
    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        super.stateChanged(toolWindowManager)

        val toolWindow = toolWindowManager.getToolWindow(RossyntToolWindow.TOOL_WINDOW_ID)
        if (toolWindow != null) {
            println("${RossyntToolWindow.TOOL_WINDOW_ID} Visible=${toolWindow.isVisible}")
        }
    }

    override fun toolWindowShown(id: String, toolWindow: ToolWindow) {
        super.toolWindowShown(id, toolWindow)

        if (id == RossyntToolWindow.TOOL_WINDOW_ID) {
            println("${RossyntToolWindow.TOOL_WINDOW_ID} Visible=${toolWindow.isVisible}")
        }
    }
}
