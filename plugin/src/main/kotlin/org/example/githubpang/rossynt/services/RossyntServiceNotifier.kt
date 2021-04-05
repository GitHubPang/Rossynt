package org.example.githubpang.rossynt.services

import com.intellij.util.messages.Topic
import org.example.githubpang.rossynt.TreeNode

internal interface RossyntServiceNotifier {
    companion object {
        val TOPIC = Topic.create("${RossyntServiceNotifier::class.qualifiedName}", RossyntServiceNotifier::class.java)
    }

    fun treeUpdated(rootTreeNode: TreeNode?)
}
