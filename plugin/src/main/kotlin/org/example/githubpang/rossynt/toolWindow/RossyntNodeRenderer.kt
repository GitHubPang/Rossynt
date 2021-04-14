package org.example.githubpang.rossynt.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.util.ui.tree.TreeUtil
import org.example.githubpang.rossynt.trees.TreeNode
import org.example.githubpang.rossynt.trees.TreeNodeCategory
import java.awt.Graphics2D
import javax.swing.JTree

/**
 * Modified from [com.intellij.ide.hierarchy.HierarchyNodeRenderer].
 */
class RossyntNodeRenderer : NodeRenderer() {
    init {
        isOpaque = false
        isIconOpaque = false
        isTransparentIconBackground = true
    }

    override fun doPaint(g: Graphics2D) {
        super.doPaint(g)
        isOpaque = false
    }

    override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        val treeNode = TreeUtil.getUserObject(TreeNode::class.java, value)
        icon = if (treeNode != null) {
            val nodeIcon = when (treeNode.treeNodeCategory) {
                TreeNodeCategory.SyntaxNode -> AllIcons.Nodes.Folder
                TreeNodeCategory.SyntaxToken -> AllIcons.Actions.Words
                TreeNodeCategory.LeadingTrivia -> AllIcons.Actions.InlayRenameInComments
                TreeNodeCategory.TrailingTrivia -> AllIcons.Actions.InlayRenameInComments
            }
            fixIconIfNeeded(nodeIcon, selected, hasFocus)
        } else {
            null
        }

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
    }
}
