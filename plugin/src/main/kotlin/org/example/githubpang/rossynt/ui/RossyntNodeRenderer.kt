package org.example.githubpang.rossynt.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.util.ui.tree.TreeUtil
import org.example.githubpang.rossynt.trees.TreeNode
import org.example.githubpang.rossynt.trees.TreeNodeType
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
        if (treeNode != null) {
            val nodeIcon = when (treeNode.Type) {
                TreeNodeType.SyntaxNode -> AllIcons.Nodes.Folder
                TreeNodeType.SyntaxToken -> AllIcons.Actions.Words
                TreeNodeType.LeadingTrivia -> AllIcons.Actions.InlayRenameInComments
                TreeNodeType.TrailingTrivia -> AllIcons.Actions.InlayRenameInComments
            }
            icon = fixIconIfNeeded(nodeIcon, selected, hasFocus)
        } else {
            icon = null
        }

        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)
    }
}
