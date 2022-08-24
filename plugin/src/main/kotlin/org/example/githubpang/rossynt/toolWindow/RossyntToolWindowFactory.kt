package org.example.githubpang.rossynt.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

internal class RossyntToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        require(toolWindow.id == RossyntToolWindow.TOOL_WINDOW_ID)

        val rossyntToolWindow = RossyntToolWindow(project, toolWindow)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(rossyntToolWindow.rootComponent, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
