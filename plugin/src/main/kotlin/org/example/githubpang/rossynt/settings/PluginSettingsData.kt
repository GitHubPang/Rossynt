package org.example.githubpang.rossynt.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@Service
@State(name = "org.example.githubpang.rossynt.settings.PluginSettingsData", storages = [Storage("Rossynt.PluginSettingsData.xml", roamingType = RoamingType.DISABLED)])
internal class PluginSettingsData : PersistentStateComponent<PluginSettingsData?> {
    companion object {
        val instance: PluginSettingsData
            get() = service()
    }

    var dotNetPath: String? = null

    // ******************************************************************************** //

    override fun getState(): PluginSettingsData = this
    override fun loadState(state: PluginSettingsData) = XmlSerializerUtil.copyBean(state, this)
}
