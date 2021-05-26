package org.example.githubpang.rossynt.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.example.githubpang.rossynt.settings.PluginSettingsNotifier
import org.example.githubpang.rossynt.trees.TreeNode

@Service
internal class RestartableBackendService : IBackendService {
    private var delegate: IBackendServiceDelegate? = null
    private var backendService: BackendService? = null
    private var backendExceptionMessage: String? = null

    // ******************************************************************************** //

    override fun startBackendService(project: Project, delegate: IBackendServiceDelegate?) {
        recreateBackendService(project)
        this.delegate = delegate

        project.messageBus.connect().subscribe(PluginSettingsNotifier.TOPIC, object : PluginSettingsNotifier {
            override fun pluginSettingsUpdated() {
                recreateBackendService(project)
            }
        })
    }

    override fun dispose() {
        delegate = null
        destroyBackendService()
    }

    override suspend fun compileFile(fileText: String?, filePath: String?): TreeNode? = backendService?.compileFile(fileText, filePath)
    override suspend fun getNodeInfo(nodeId: String): Map<String, String> = backendService?.getNodeInfo(nodeId) ?: HashMap()

    private fun recreateBackendService(project: Project) {
        destroyBackendService()

        backendService = BackendService()
        backendService?.startBackendService(project, object : IBackendServiceDelegate {
            override fun onBackendExceptionMessageUpdated(backendExceptionMessage: String?) {
                setBackendExceptionMessage(backendExceptionMessage)
            }
        })
    }

    private fun destroyBackendService() {
        backendService?.dispose()
        backendService = null
        setBackendExceptionMessage(null)
    }

    private fun setBackendExceptionMessage(backendExceptionMessage: String?) {
        if (this.backendExceptionMessage == backendExceptionMessage) {
            return
        }
        this.backendExceptionMessage = backendExceptionMessage

        // Inform delegate.
        delegate?.onBackendExceptionMessageUpdated(backendExceptionMessage)
    }
}
