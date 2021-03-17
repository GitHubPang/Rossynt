package org.jetbrains.plugins.template.listeners

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import org.jetbrains.plugins.template.toolWindow.MyToolWindow

internal class MyToolWindowManagerListener : ToolWindowManagerListener {
    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        super.stateChanged(toolWindowManager)

        val toolWindow = toolWindowManager.getToolWindow(MyToolWindow.TOOL_WINDOW_ID)
        if (toolWindow != null) {
            println("${MyToolWindow.TOOL_WINDOW_ID} Visible=${toolWindow.isVisible}")
        }
    }

    override fun toolWindowShown(id: String, toolWindow: ToolWindow) {
        super.toolWindowShown(id, toolWindow)

        if (id == MyToolWindow.TOOL_WINDOW_ID) {
            println("${MyToolWindow.TOOL_WINDOW_ID} Visible=${toolWindow.isVisible}")
        }
    }
}
