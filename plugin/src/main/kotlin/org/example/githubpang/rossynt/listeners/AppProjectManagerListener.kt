package org.example.githubpang.rossynt.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.example.githubpang.rossynt.services.RossyntService

internal class AppProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        super.projectOpened(project)

        // Start Rossynt service.
        val rossyntService = project.service<RossyntService>()
        rossyntService.startRossyntService(project)
    }
}
