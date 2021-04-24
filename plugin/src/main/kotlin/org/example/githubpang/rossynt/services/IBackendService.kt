package org.example.githubpang.rossynt.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.example.githubpang.rossynt.trees.TreeNode

internal interface IBackendService : Disposable {
    fun startBackendService(project: Project)
    suspend fun compileFile(fileText: String?, filePath: String?): TreeNode?
    suspend fun getNodeInfo(nodeId: String): Map<String, String>
}
