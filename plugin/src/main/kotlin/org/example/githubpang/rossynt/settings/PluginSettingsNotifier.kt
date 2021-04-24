package org.example.githubpang.rossynt.settings

import com.intellij.util.messages.Topic

internal interface PluginSettingsNotifier {
    companion object {
        val TOPIC = Topic.create("${PluginSettingsNotifier::class.qualifiedName}", PluginSettingsNotifier::class.java)
    }

    fun pluginSettingsUpdated()
}
