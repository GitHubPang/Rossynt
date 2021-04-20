package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList
import com.google.gson.annotations.SerializedName
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.editor.colors.CodeInsightColors
import org.example.githubpang.rossynt.AppIcons

internal data class TreeNode(
    @SerializedName("Id") val nodeId: String,
    @SerializedName("Cat") val treeNodeCategory: TreeNodeCategory,
    @SerializedName("Type") val rawType: String,
    @SerializedName("Kind") val syntaxKind: String,
    @SerializedName("Str") val shortString: String,
    @SerializedName("IsMissing") private val isMissingInt: Int,
    @SerializedName("Child") private val childTreeNodes: List<TreeNode>?
) : NavigationItem {
    private val isMissing: Boolean
        get() {
            return isMissingInt == 1
        }

    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (childTreeNodes != null) {
            ImmutableList.copyOf(childTreeNodes)
        } else
            ImmutableList.of()
    }

    override fun navigate(requestFocus: Boolean): Unit = Unit
    override fun canNavigate(): Boolean = false
    override fun canNavigateToSource(): Boolean = false
    override fun getName(): String? = null
    override fun getPresentation(): ItemPresentation = PresentationData(
        syntaxKind,
        shortString, //todo what about line breaks? white spaces? null characters?
        when (treeNodeCategory) {
            TreeNodeCategory.SyntaxNode -> AppIcons.TreeNodeCategorySyntaxNode
            TreeNodeCategory.SyntaxToken -> AppIcons.TreeNodeCategorySyntaxToken
            TreeNodeCategory.LeadingTrivia -> AppIcons.TreeNodeCategoryTrivia
            TreeNodeCategory.TrailingTrivia -> AppIcons.TreeNodeCategoryTrivia
        },
        if (isError()) CodeInsightColors.ERRORS_ATTRIBUTES else null
    )

    /**
     * @see <a href="https://docs.microsoft.com/en-us/dotnet/csharp/roslyn-sdk/work-with-syntax#errors">Microsoft Docs | Use the .NET Compiler Platform SDK syntax model | Errors</a>
     */
    private fun isError(): Boolean {
        if (isMissing || syntaxKind == "SkippedTokensTrivia") {
            return true
        }

        return childTreeNodes != null && childTreeNodes.any { it.isError() }
    }
}
