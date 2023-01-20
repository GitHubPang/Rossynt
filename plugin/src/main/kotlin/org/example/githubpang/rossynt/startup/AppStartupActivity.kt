package org.example.githubpang.rossynt.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.example.githubpang.rossynt.services.RossyntService

internal class AppStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // Start Rossynt service if needed.
        val rossyntService = project.service<RossyntService>()
        rossyntService.startRossyntServiceIfNeeded(project)
    }
}
