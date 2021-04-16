package org.example.githubpang.rossynt.services

import com.google.common.collect.ImmutableMap
import com.intellij.util.messages.Topic
import org.example.githubpang.rossynt.trees.TreeNode

internal interface RossyntServiceNotifier {
    companion object {
        val TOPIC = Topic.create("${RossyntServiceNotifier::class.qualifiedName}", RossyntServiceNotifier::class.java)
    }

    fun treeUpdated(rootTreeNode: TreeNode?)
    fun nodeInfoUpdated(nodeInfo: ImmutableMap<String, String>?)
}
