package org.example.githubpang.rossynt

import com.google.common.collect.ImmutableList

internal data class TreeNode(val Id: String, val Type: String, val Kind: String, private val Child: List<TreeNode>?) {
    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (Child != null) {
            ImmutableList.copyOf(Child)
        } else
            ImmutableList.of()
    }

    override fun toString(): String {
        return Kind
    }
}
