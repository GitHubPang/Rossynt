package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList

internal data class TreeNode(val Id: String, val Type: TreeNodeType, val Kind: String, private val Child: List<TreeNode>?) {
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
