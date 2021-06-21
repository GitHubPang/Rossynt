package org.example.githubpang.rossynt.trees

import com.google.common.collect.ImmutableList
import com.google.gson.annotations.SerializedName
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.util.TextRange
import icons.PluginIcons
import org.apache.commons.lang3.StringUtils

internal data class TreeNode(
    @SerializedName("Id") val nodeId: String,
    @SerializedName("Cat") val treeNodeCategory: TreeNodeCategory,
    @SerializedName("Type") val rawType: String,
    @SerializedName("Kind") val syntaxKind: String,
    @SerializedName("Str") private val str: String?,
    @SerializedName("Span") private val span: String?,
    @SerializedName("IsMissing") private val isMissingInt: Int,
    @SerializedName("Child") private val childTreeNodes: List<TreeNode>?
) : NavigationItem {
    private val shortString: String
        get() {
            return str ?: ""
        }

    val textRange: TextRange?
        get() {
            return if (span != null) {
                val components = StringUtils.split(span, ',')
                val start = components[0].toInt()
                val length = if (components.size > 1) components[1].toInt() else 1
                return TextRange.from(start, length)
            } else {
                null
            }
        }

    private val isMissing: Boolean
        get() {
            return isMissingInt == 1
        }

    fun childTreeNodes(): ImmutableList<TreeNode> {
        return if (childTreeNodes != null) {
            ImmutableList.copyOf(childTreeNodes)
        } else {
            ImmutableList.of()
        }
    }

    override fun navigate(requestFocus: Boolean): Unit = Unit
    override fun canNavigate(): Boolean = false
    override fun canNavigateToSource(): Boolean = false
    override fun getName(): String? = null
    override fun getPresentation(): ItemPresentation = PresentationData(
        syntaxKind,
        shortString, // todo what about line breaks? white spaces? null characters?
        when (treeNodeCategory) {
            TreeNodeCategory.SyntaxNode -> PluginIcons.TreeNodeCategorySyntaxNode
            TreeNodeCategory.SyntaxToken -> PluginIcons.TreeNodeCategorySyntaxToken
            TreeNodeCategory.LeadingTrivia -> PluginIcons.TreeNodeCategoryTrivia
            TreeNodeCategory.TrailingTrivia -> PluginIcons.TreeNodeCategoryTrivia
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
