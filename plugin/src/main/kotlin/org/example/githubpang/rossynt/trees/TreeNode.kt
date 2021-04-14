package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList
import com.google.gson.annotations.SerializedName

internal data class TreeNode(
    @SerializedName("Id") val nodeId: String,
    @SerializedName("Str") val str: String,
    @SerializedName("Cat") val treeNodeCategory: TreeNodeCategory,
    @SerializedName("Kind") val kind: String,
    @SerializedName("Child") private val childNodes: List<TreeNode>?
) {
    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (childNodes != null) {
            ImmutableList.copyOf(childNodes)
        } else
            ImmutableList.of()
    }

    //todo better not override toString?
    override fun toString(): String {
        //todo what about line breaks? white spaces?
        return kind + " " + str
    }
}
