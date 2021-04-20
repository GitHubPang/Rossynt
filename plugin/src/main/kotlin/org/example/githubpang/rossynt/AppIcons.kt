package org.example.githubpang.rossynt

import com.intellij.ui.IconManager
import javax.swing.Icon

/**
 * Modified from [com.intellij.icons.AllIcons].
 */
internal object AppIcons {
    private fun load(path: String): Icon {
        return IconManager.getInstance().getIcon(path, AppIcons::class.java)
    }

    // ******************************************************************************** //

    val TreeNodeCategorySyntaxNode = load("/icons/treeNodeCategorySyntaxNode.svg")
    val TreeNodeCategorySyntaxToken = load("/icons/treeNodeCategorySyntaxToken.svg")
    val TreeNodeCategoryTrivia = load("/icons/treeNodeCategoryTrivia.svg")
}
