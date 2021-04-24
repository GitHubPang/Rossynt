package org.example.githubpang.rossynt.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.githubpang.rossynt.settings.PluginSettingsNotifier
import org.example.githubpang.rossynt.trees.TreeNode

@Service
internal class RestartableBackendService : IBackendService {
    private var backendService: BackendService? = null

    // ******************************************************************************** //

    override fun startBackendService(project: Project) {
        recreateBackendService(project)

        project.messageBus.connect().subscribe(PluginSettingsNotifier.TOPIC, object : PluginSettingsNotifier {
            override fun pluginSettingsUpdated() {
                GlobalScope.launch(Dispatchers.Main) {
                    recreateBackendService(project)
                }
            }
        })
    }

    override fun dispose() {
        destroyBackendService()
    }

    override suspend fun compileFile(fileText: String?, filePath: String?): TreeNode? = backendService?.compileFile(fileText, filePath)
    override suspend fun getNodeInfo(nodeId: String): Map<String, String> = backendService?.getNodeInfo(nodeId) ?: HashMap()

    private fun recreateBackendService(project: Project) {
        destroyBackendService()

        backendService = BackendService()
        backendService?.startBackendService(project)
    }

    private fun destroyBackendService() {
        backendService?.dispose()
        backendService = null
    }
}
