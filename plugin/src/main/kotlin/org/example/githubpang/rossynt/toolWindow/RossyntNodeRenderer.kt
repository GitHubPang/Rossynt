package org.example.githubpang.rossynt.toolWindow

import com.intellij.ide.util.treeView.NodeRenderer
import java.awt.Graphics2D

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
}
