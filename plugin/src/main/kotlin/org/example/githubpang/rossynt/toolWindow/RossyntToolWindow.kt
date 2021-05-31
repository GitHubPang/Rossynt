package org.example.githubpang.rossynt.toolWindow

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.ui.treeStructure.actions.CollapseAllAction
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.tree.TreeUtil
import org.example.githubpang.rossynt.services.IRossyntService
import org.example.githubpang.rossynt.services.RossyntService
import org.example.githubpang.rossynt.services.RossyntServiceNotifier
import org.example.githubpang.rossynt.services.RossyntUtil
import org.example.githubpang.rossynt.settings.PluginSettingsConfigurable
import org.example.githubpang.rossynt.trees.TreeNode
import java.awt.Component
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

internal class RossyntToolWindow(private val project: Project, toolWindow: ToolWindow) {
    companion object {
        const val TOOL_WINDOW_ID: String = "Rossynt"  // Must match with toolWindow id in "plugin.xml"
    }

    private enum class SpecialRow {
        RawType,
        SyntaxKind,
    }

    // ******************************************************************************** //

    private inner class UiTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = this@RossyntToolWindow.nodeInfo.size + when (this@RossyntToolWindow.selectedTreeNode) {
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

            @Suppress("NAME_SHADOWING")
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

            return this@RossyntToolWindow.nodeInfo[rowIndex]
        }
    }

    private class UiTableCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (row < SpecialRow.values().size) {
                component.font = component.font.deriveFont(Font.BOLD)
            }

            return component
        }
    }

    private inner class ToggleHighlightNodeInSourceAction : ToggleAction("Highlight Node in Source", null, AllIcons.Actions.Highlighting), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean {
            return isHighlightSelectedTreeNode
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (isHighlightSelectedTreeNode == state) {
                return
            }
            isHighlightSelectedTreeNode = state

            // Update highlight.
            updateRangeHighlighters()
        }
    }

    // ******************************************************************************** //

    // Data.
    private val rossyntService: RossyntService = project.service()
    private var currentFilePath: String? = null
    private var rootTreeNode: TreeNode? = null
    private var selectedTreeNode: TreeNode? = null
    private var backendExceptionMessage: String? = null
    private var nodeInfo: ImmutableList<Pair<String, String>> = ImmutableList.of()
    private var isHighlightSelectedTreeNode = false

    // UI.
    private val uiTree: Tree = Tree()
    private val uiBanner = EditorNotificationPanel(MessageType.ERROR.popupBackground)
    private val uiTable: JTable = JTable(UiTableModel())
    private val uiSplitter: JBSplitter = JBSplitter()
    val rootComponent: JComponent
        get() {
            return JBUI.Panels.simplePanel(uiSplitter).addToTop(uiBanner)
        }
    private var myRangeHighlighter: RangeHighlighter? = null

    // ******************************************************************************** //

    init {
        currentFilePath = rossyntService.getCurrentFilePath()
        rossyntService.setDelegate(object : IRossyntService {
            override fun onCurrentFilePathUpdated(filePath: String?) {
                if (currentFilePath == filePath) {
                    return
                }

                currentFilePath = filePath

                // Update UI.
                uiUpdateTreeEmptyText()
            }
        })

        // Add tool window buttons.
        val collapseAction = CollapseAllAction(uiTree)
        collapseAction.templatePresentation.icon = AllIcons.Actions.Collapseall
        toolWindow.setTitleActions(listOf(ToggleHighlightNodeInSourceAction(), collapseAction))

        // Setup banner.
        uiBanner.text = "Error occurred"
        uiBanner.createActionLabel(CommonBundle.settingsTitle(), {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginSettingsConfigurable::class.java)
        }, true)

        // Setup tree.
        if (uiTree.cellRenderer !is RossyntNodeRenderer) {
            uiTree.cellRenderer = RossyntNodeRenderer()
        }
        uiTree.selectionModel.selectionMode = SINGLE_TREE_SELECTION
        uiTree.addTreeSelectionListener {
            val treeNode = TreeUtil.getUserObject(TreeNode::class.java, uiTree.lastSelectedPathComponent)
            selectedTreeNode = treeNode
            rossyntService.setCurrentNodeId(treeNode?.nodeId)

            // Update highlight.
            updateRangeHighlighters()

            // Update UI.
            uiUpdateTable()
        }

        // Setup table.
        uiTable.tableHeader = null
        uiTable.setDefaultRenderer(Object::class.java, UiTableCellRenderer())

        // Setup splitter.
        uiSplitter.orientation = true
        uiSplitter.firstComponent = ScrollPaneFactory.createScrollPane(uiTree, true)
        uiSplitter.secondComponent = ScrollPaneFactory.createScrollPane(uiTable, true)

        // Subscribe messages.
        project.messageBus.connect().subscribe(RossyntServiceNotifier.TOPIC, object : RossyntServiceNotifier {
            override fun treeUpdated(rootTreeNode: TreeNode?) {
                this@RossyntToolWindow.rootTreeNode = rootTreeNode
                selectedTreeNode = null

                // Update highlight.
                updateRangeHighlighters()

                // Update UI.
                uiUpdateTree()
            }

            override fun nodeInfoUpdated(nodeInfo: ImmutableMap<String, String>?) {
                this@RossyntToolWindow.nodeInfo = when {
                    nodeInfo != null -> ImmutableList.copyOf(nodeInfo.toList().sortedBy { pair -> pair.first })
                    else -> ImmutableList.of()
                }

                // Update UI.
                uiUpdateTable()
            }

            override fun backendExceptionMessageUpdated(backendExceptionMessage: String?) {
                if (this@RossyntToolWindow.backendExceptionMessage == backendExceptionMessage) {
                    return
                }
                this@RossyntToolWindow.backendExceptionMessage = backendExceptionMessage

                // Update UI.
                uiUpdateBanner()
            }
        })

        // Update UI.
        uiUpdateBanner()
        uiUpdateTree()
        uiUpdateTreeEmptyText()
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

    private fun uiUpdateBanner() {
        val backendExceptionMessage = backendExceptionMessage
        if (backendExceptionMessage != null) {
            uiBanner.isVisible = true

            HelpTooltip.dispose(uiBanner)
            HelpTooltip().setDescription(backendExceptionMessage).installOn(uiBanner)
        } else {
            uiBanner.isVisible = false
        }
    }

    private fun uiUpdateTreeEmptyText() {
        val defaultEmptyText = StatusText.getDefaultEmptyText()

        val currentFilePath = currentFilePath
        if (currentFilePath != null && !RossyntUtil.isCSFile(currentFilePath)) {
            uiTree.emptyText.text = "$defaultEmptyText - not a C# file"
        } else {
            uiTree.emptyText.text = defaultEmptyText
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

    private fun updateRangeHighlighters() {
        // Remove old highlight.
        removeRangeHighlighters()

        // Add new highlight.
        val textRange = selectedTreeNode?.textRange
        if (isHighlightSelectedTreeNode && textRange != null) {
            myRangeHighlighter = FileEditorManager.getInstance(project).selectedTextEditor?.markupModel?.addRangeHighlighter(
                EditorColors.SEARCH_RESULT_ATTRIBUTES, textRange.startOffset, textRange.endOffset, HighlighterLayer.LAST + 1,
                HighlighterTargetArea.EXACT_RANGE
            )
        }
    }

    private fun removeRangeHighlighters() {
        myRangeHighlighter?.dispose()
        myRangeHighlighter = null
    }
}
