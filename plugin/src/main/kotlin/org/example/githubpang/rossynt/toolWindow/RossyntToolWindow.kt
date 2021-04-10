package org.example.githubpang.rossynt.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.example.githubpang.rossynt.services.RossyntServiceNotifier
import org.example.githubpang.rossynt.trees.TreeNode
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

internal class RossyntToolWindow(project: Project) {
    companion object {
        const val TOOL_WINDOW_ID: String = "Rossynt"  // Must match with toolWindow id in "plugin.xml"
    }

    // ******************************************************************************** //

    private class MyTableModel : AbstractTableModel() {
        override fun getRowCount(): Int {
            return 25
        }

        override fun getColumnCount(): Int {
            return 2
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return "Hello World Hello World Hello World"
        }
    }

    // ******************************************************************************** //

    // Data.
    private var rootTreeNode: TreeNode? = null

    // UI.
    private val uiTree: Tree = Tree()
    private val uiTable: JTable = JTable(MyTableModel())
    private val uiSplitter: JBSplitter = JBSplitter()
    val rootComponent: JComponent
        get() {
            return uiSplitter
        }

    // ******************************************************************************** //

    init {
        // Setup tree.
        if (uiTree.cellRenderer !is RossyntNodeRenderer) {
            uiTree.cellRenderer = RossyntNodeRenderer()
        }
        uiTree.selectionModel.selectionMode = SINGLE_TREE_SELECTION
        uiTree.addTreeSelectionListener {
            val treeNode = TreeUtil.getUserObject(TreeNode::class.java, uiTree.lastSelectedPathComponent)
            if (treeNode == null) {
                println("No selection")
                return@addTreeSelectionListener
            }

            println("Selected treeNode: $treeNode")
        }

        // Setup table.
        uiTable.tableHeader = null

        // Setup splitter.
        uiSplitter.orientation = true
        uiSplitter.firstComponent = ScrollPaneFactory.createScrollPane(uiTree, true)
        uiSplitter.secondComponent = ScrollPaneFactory.createScrollPane(uiTable, true)

        // Subscribe messages.
        project.messageBus.connect().subscribe(RossyntServiceNotifier.TOPIC, object : RossyntServiceNotifier {
            override fun treeUpdated(rootTreeNode: TreeNode?) {
                runBlocking(Dispatchers.Main) {
                    this@RossyntToolWindow.rootTreeNode = rootTreeNode

                    uiUpdateTree()
                }
            }
        })

        // Update UI.
        uiUpdateTree()
    }

    private fun uiUpdateTree() {
        val rootTreeNode = rootTreeNode

        val uiModel = uiTree.model as DefaultTreeModel
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
