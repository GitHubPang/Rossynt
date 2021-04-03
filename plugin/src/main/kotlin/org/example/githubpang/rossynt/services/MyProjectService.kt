package org.example.githubpang.rossynt.services

import com.intellij.openapi.project.Project
import org.example.githubpang.rossynt.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
