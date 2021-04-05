package org.example.githubpang.rossynt

import com.intellij.util.messages.Topic

internal interface RossyntToolWindowStateNotifier {
    companion object {
        val TOPIC = Topic.create("${RossyntToolWindowStateNotifier::class.qualifiedName}", RossyntToolWindowStateNotifier::class.java)
    }

    fun rossyntToolWindowIsVisibleUpdated(isVisible: Boolean)
}
