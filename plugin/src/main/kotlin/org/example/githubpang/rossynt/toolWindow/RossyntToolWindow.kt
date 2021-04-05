package org.example.githubpang.rossynt.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.Tree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.example.githubpang.rossynt.TreeNode
import org.example.githubpang.rossynt.services.RossyntServiceNotifier
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

internal class RossyntToolWindow(project: Project) {
    companion object {
        const val TOOL_WINDOW_ID: String = "Rossynt"  // Must match with toolWindow id in "plugin.xml"
    }

    private var rootTreeNode: TreeNode? = null

    private var uiTree: Tree? = null
    private var labelStatusMessage: JLabel? = null
    private var buttonTest: JButton? = null
    var content: JPanel? = null

    // ******************************************************************************** //

    init {
        project.messageBus.connect().subscribe(RossyntServiceNotifier.TOPIC, object : RossyntServiceNotifier {
            override fun treeUpdated(rootTreeNode: TreeNode?) {
                runBlocking(Dispatchers.Main) {
                    this@RossyntToolWindow.rootTreeNode = rootTreeNode

                    uiUpdateTree()
                }
            }
        })

        buttonTest!!.addActionListener {
        }

        uiUpdateTree()
    }

    private fun uiUpdateTree() {
//        labelStatusMessage!!.text = """$currentDate $currentFilePath"""
//        labelStatusMessage!!.icon = ImageIcon(javaClass.getResource("/toolWindow/Time-icon.png"))
        val rootTreeNode = rootTreeNode

        val uiModel = uiTree!!.model as DefaultTreeModel
        if (rootTreeNode != null) {
            val uiRoot = createUiNode(null, rootTreeNode)
            uiModel.setRoot(uiRoot)
            uiModel.reload(uiRoot)
        } else {
            uiModel.setRoot(null)
        }
    }

    private fun createUiNode(uiParentNode: DefaultMutableTreeNode?, treeNode: TreeNode): DefaultMutableTreeNode {
        val uiNode = DefaultMutableTreeNode(treeNode)
        uiParentNode?.add(uiNode)

        treeNode.childTreeNodes().forEach { childTreeNode ->
            createUiNode(uiNode, childTreeNode)
        }

        return uiNode
    }
}
