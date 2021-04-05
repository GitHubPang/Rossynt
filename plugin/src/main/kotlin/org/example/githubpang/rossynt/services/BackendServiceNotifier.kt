package org.example.githubpang.rossynt.services

import com.intellij.util.messages.Topic

internal interface BackendServiceNotifier {
    companion object {
        val TOPIC = Topic.create("${BackendServiceNotifier::class.qualifiedName}", BackendServiceNotifier::class.java)
    }

    fun backendServiceBecameReady()
}
