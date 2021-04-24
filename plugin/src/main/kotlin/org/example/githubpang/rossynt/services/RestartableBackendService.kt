package org.example.githubpang.rossynt.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.example.githubpang.rossynt.trees.TreeNode

@Service
internal class RestartableBackendService : IBackendService {
    private var backendService: BackendService? = null

    // ******************************************************************************** //

    override fun startBackendService(project: Project) {
        backendService?.dispose()
        backendService = BackendService()
        backendService?.startBackendService(project)
    }

    override fun dispose() {
        backendService?.dispose()
        backendService = null
    }

    override suspend fun compileFile(fileText: String?, filePath: String?): TreeNode? = backendService?.compileFile(fileText, filePath)
    override suspend fun getNodeInfo(nodeId: String): Map<String, String> = backendService?.getNodeInfo(nodeId) ?: HashMap()
}
