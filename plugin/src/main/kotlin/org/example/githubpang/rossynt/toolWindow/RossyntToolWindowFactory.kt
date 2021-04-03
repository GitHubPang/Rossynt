package org.example.githubpang.rossynt.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.example.githubpang.rossynt.services.BackendService


class RossyntToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        require(toolWindow.id == RossyntToolWindow.TOOL_WINDOW_ID)

        project.service<BackendService>() // Load backend service.//todo any better place to start service?

        val rossyntToolWindow = RossyntToolWindow(project.messageBus)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(rossyntToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
