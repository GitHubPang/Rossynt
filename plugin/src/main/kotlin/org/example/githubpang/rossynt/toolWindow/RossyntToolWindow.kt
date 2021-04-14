package org.example.githubpang.rossynt.toolWindow

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import org.example.githubpang.rossynt.services.RossyntService
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

    private enum class SpecialRow {
        RawType,
        SyntaxKind,
    }

    // ******************************************************************************** //

    private inner class UiTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = this@RossyntToolWindow.currentNodeInfo.size + when (this@RossyntToolWindow.selectedTreeNode) {
            null -> 0
            else -> SpecialRow.values().size
        }

        override fun getColumnCount(): Int = 2

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val rowValue = getRowValueAt(rowIndex)
            return when (columnIndex) {
                0 -> rowValue.first
                1 -> rowValue.second
                else -> ""
            }
        }

        private fun getRowValueAt(rowIndex: Int): Pair<String, String> {
            val selectedTreeNode = this@RossyntToolWindow.selectedTreeNode
            var rowIndex = rowIndex

            if (selectedTreeNode != null) {
                val specialRows = SpecialRow.values()

                if (rowIndex < specialRows.size) {
                    return when (specialRows[rowIndex]) {
                        SpecialRow.RawType -> Pair("Type", selectedTreeNode.rawType)
                        SpecialRow.SyntaxKind -> Pair("Kind", selectedTreeNode.syntaxKind)
                    }
                }

                rowIndex -= specialRows.size
            }

            return this@RossyntToolWindow.currentNodeInfo[rowIndex]
        }
    }

    // ******************************************************************************** //

    // Data.
    private val rossyntService: RossyntService = project.service()
    private var rootTreeNode: TreeNode? = null
    private var selectedTreeNode: TreeNode? = null
    private var currentNodeInfo: ImmutableList<Pair<String, String>> = ImmutableList.of()

    // UI.
    private val uiTree: Tree = Tree()
    private val uiTable: JTable = JTable(UiTableModel())
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
            selectedTreeNode = treeNode
            rossyntService.setCurrentNodeId(treeNode?.nodeId)

            // Update UI.
            uiUpdateTable()
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
                this@RossyntToolWindow.rootTreeNode = rootTreeNode
                selectedTreeNode = null

                // Update UI.
                uiUpdateTree()
            }

            override fun currentNodeInfoUpdated(currentNodeInfo: ImmutableMap<String, String>?) {
                this@RossyntToolWindow.currentNodeInfo = when {
                    currentNodeInfo != null -> ImmutableList.copyOf(currentNodeInfo.toList().sortedBy { pair -> pair.first })
                    else -> ImmutableList.of()
                }

                // Update UI.
                uiUpdateTable()
            }
        })

        // Update UI.
        uiUpdateTree()
        uiUpdateTable()
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

    private fun uiUpdateTable() {
        (uiTable.model as AbstractTableModel).fireTableDataChanged()
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
