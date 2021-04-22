/*
Package must be named "icons" and icons must be decorated with "@JvmField" so that they can be used in plugin.xml.

Reference: "How to organize and how to use icons?"
https://plugins.jetbrains.com/docs/intellij/work-with-icons-and-images.html#how-to-organize-and-how-to-use-icons
*/
@file:Suppress("PackageDirectoryMismatch")

package icons

import com.intellij.ui.IconManager
import javax.swing.Icon

/**
 * Modified from [com.intellij.icons.AllIcons].
 */
internal object PluginIcons {
    private fun load(path: String): Icon {
        return IconManager.getInstance().getIcon(path, PluginIcons::class.java)
    }

    // ******************************************************************************** //

    @Suppress("unused")
    @JvmField
    val ToolWindowIcon = load("/icons/toolWindowIcon.svg")

    val TreeNodeCategorySyntaxNode = load("/icons/treeNodeCategorySyntaxNode.svg")
    val TreeNodeCategorySyntaxToken = load("/icons/treeNodeCategorySyntaxToken.svg")
    val TreeNodeCategoryTrivia = load("/icons/treeNodeCategoryTrivia.svg")
}
