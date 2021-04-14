package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList
import com.google.gson.annotations.SerializedName

internal data class TreeNode(
    @SerializedName("Id") val nodeId: String,
    @SerializedName("Cat") val treeNodeCategory: TreeNodeCategory,
    @SerializedName("Type") val rawType: String,
    @SerializedName("Kind") val syntaxKind: String,
    @SerializedName("Str") val shortString: String,
    @SerializedName("Child") private val childTreeNodes: List<TreeNode>?
) {
    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (childTreeNodes != null) {
            ImmutableList.copyOf(childTreeNodes)
        } else
            ImmutableList.of()
    }

    //todo better not override toString?
    override fun toString(): String {
        //todo what about line breaks? white spaces?
        return syntaxKind + " " + shortString
    }
}
