package org.example.githubpang.rossynt.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Service
@State(name = "org.example.githubpang.rossynt.settings.PluginSettingsData", storages = [Storage("Rossynt.PluginSettingsData.xml", roamingType = RoamingType.DISABLED)])
internal class PluginSettingsData : PersistentStateComponent<PluginSettingsData?> {
    companion object {
        val instance: PluginSettingsData
            get() = service()
    }

    var dotNetPath: String? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value

            GlobalScope.launch(Dispatchers.Main) {
                // Publish message.
                val messageBus = ApplicationManager.getApplication()?.messageBus
                val publisher = messageBus?.syncPublisher(PluginSettingsNotifier.TOPIC)
                publisher?.pluginSettingsUpdated()
            }
        }

    // ******************************************************************************** //

    override fun getState(): PluginSettingsData = this
    override fun loadState(state: PluginSettingsData) = XmlSerializerUtil.copyBean(state, this)
}
