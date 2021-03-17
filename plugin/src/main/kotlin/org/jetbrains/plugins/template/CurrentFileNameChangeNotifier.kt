package org.jetbrains.plugins.template

import com.intellij.util.messages.Topic

interface CurrentFileNameChangeNotifier {
    companion object {
        val TOPIC = Topic.create("${CurrentFileNameChangeNotifier::class.qualifiedName}", CurrentFileNameChangeNotifier::class.java)
    }

    fun currentFileNameChanged(filePath: String?)
}
