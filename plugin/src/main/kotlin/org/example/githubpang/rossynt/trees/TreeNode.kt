package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList

internal data class TreeNode(val Id: String, val Str: String, val Type: TreeNodeType, val Kind: String, private val Child: List<TreeNode>?) {
    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (Child != null) {
            ImmutableList.copyOf(Child)
        } else
            ImmutableList.of()
    }

    //todo better not override toString?
    override fun toString(): String {
        //todo what about line breaks? white spaces?
        return Kind + " " + Str
    }
}
