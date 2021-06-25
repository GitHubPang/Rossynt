package org.example.githubpang.rossynt.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

internal class PluginSettingsConfigurable : Configurable {
    private var ui: PluginSettingsUi? = null

    // ******************************************************************************** //

    override fun createComponent(): JComponent {
        val ui = PluginSettingsUi()
        this.ui = ui
        return ui.rootComponent
    }

    override fun disposeUIResources() {
        ui = null
        super.disposeUIResources()
    }

    override fun isModified(): Boolean {
        return ui?.dotNetPath != PluginSettingsData.instance.dotNetPath
    }

    override fun apply() {
        PluginSettingsData.instance.dotNetPath = ui?.dotNetPath
    }

    override fun reset() {
        ui?.dotNetPath = PluginSettingsData.instance.dotNetPath
    }

    override fun getDisplayName(): String {
        return "Rossynt"
    }
}
