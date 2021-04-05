package org.example.githubpang.rossynt

import com.intellij.util.messages.Topic

internal interface CurrentFileNotifier {
    companion object {
        val TOPIC = Topic.create("${CurrentFileNotifier::class.qualifiedName}", CurrentFileNotifier::class.java)
    }

    fun currentFileChanged(filePath: String?)
}
