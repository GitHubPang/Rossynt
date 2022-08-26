package org.example.githubpang.rossynt.toolWindow

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.*
import com.intellij.ui.table.JBTable
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import org.example.githubpang.rossynt.services.CSharpVersion
import org.example.githubpang.rossynt.services.IRossyntService
import org.example.githubpang.rossynt.services.RossyntService
import org.example.githubpang.rossynt.services.RossyntServiceNotifier
import org.example.githubpang.rossynt.services.RossyntUtil
import org.example.githubpang.rossynt.settings.PluginSettingsConfigurable
import org.example.githubpang.rossynt.trees.SyntaxUtil
import org.example.githubpang.rossynt.trees.TreeNode
import java.awt.Component
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.JTree
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

internal class RossyntToolWindow(private val project: Project, toolWindow: ToolWindow) {
    companion object {
        const val TOOL_WINDOW_ID: String = "Rossynt"  // Must match with toolWindow id in "plugin.xml"
        val ERROR_ICON = UIUtil.getBalloonErrorIcon()
    }

    // ******************************************************************************** //

    private enum class SpecialRow {
        RawType,
        SyntaxKind,
    }

    private inner class UiTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = nodeInfo.size + when (selectedTreeNode) {
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
            val selectedTreeNode = selectedTreeNode

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

            return nodeInfo[rowIndex]
        }
    }

    private class UiTableCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (row < SpecialRow.values().size) {
                component.font = component.font.deriveFont(Font.BOLD)
            }

            val label = component as JLabel
            label.icon = if (isError(table, value, row, column)) {
                ERROR_ICON
            } else {
                null
            }

            return component
        }

        private fun isError(table: JTable?, value: Any?, row: Int, column: Int): Boolean {
            if (column == 1) {
                val key = table?.model?.getValueAt(row, 0)

                // The following conditions need to be in sync with
                // org.example.githubpang.rossynt.trees.TreeNode.isError
                if (key == "IsMissing" && value == "True") {
                    return true
                }
                if (key == "Kind" && value is String && SyntaxUtil.isSyntaxKindError(value)) {
                    return true
                }
            }

            return false
        }
    }

    private class RossyntTreeExpander(tree: JTree) : DefaultTreeExpander(tree) {
        override fun collapseAll(tree: JTree, keepSelectionLevel: Int) {
            super.collapseAll(tree, 2)
            TreeUtil.expand(tree, 1)
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

    private inner class SelectNodeAtCaretAction : AnAction("Select Node at Caret", null, AllIcons.General.Locate), DumbAware {
        override fun update(e: AnActionEvent) {
            super.update(e)

            e.presentation.isEnabled = rootTreeNode != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            rossyntService.findNodeAtCaret()
        }
    }

    private inner class CSharpVersionChooserAction : ComboBoxAction(), DumbAware {
        private inner class CSharpVersionAction(cSharpVersion: CSharpVersion) : AnAction(cSharpVersion.name) {
            override fun actionPerformed(e: AnActionEvent) {
                TODO("Not yet implemented")
            }
        }

        override fun createPopupActionGroup(button: JComponent?): DefaultActionGroup {
            val defaultActionGroup = DefaultActionGroup()
            CSharpVersion.values().forEach { defaultActionGroup.add(CSharpVersionAction(it)) }
            return defaultActionGroup
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
    private val uiTable: JBTable = JBTable(UiTableModel())
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

            override fun onFindNodeAtCaretResult(nodeId: String?) {
                if (nodeId == null) {
                    return
                }

                val uiNode = TreeUtil.findNode(uiTree.model.root as DefaultMutableTreeNode) { (it.userObject as TreeNode).nodeId == nodeId } ?: return
                TreeUtil.selectPath(uiTree, TreePath(uiNode.path))
            }
        })

        // Add tool window buttons.
        val collapseAction = CommonActionsManager.getInstance().createCollapseAllAction(RossyntTreeExpander(uiTree), uiTree)
        toolWindow.setTitleActions(listOf(ToggleHighlightNodeInSourceAction(), SelectNodeAtCaretAction(), collapseAction, CSharpVersionChooserAction()))

        // Setup banner.
        uiBanner.text = "Error occurred"
        val settingsAction = DialogWrapper.extractMnemonic(CommonBundle.settingsAction()).second
        uiBanner.createActionLabel(settingsAction, {
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
            //todo scroll selected node into view when double click, like Structure view in IDEA/Rider. Or even better, add a Navigate with Single Click button, then react to single click
        }
        TreeSpeedSearch(uiTree, { it.lastPathComponent.toString() }, true)

        // Setup table.
        uiTable.tableHeader = null
        uiTable.setDefaultRenderer(Object::class.java, UiTableCellRenderer())
        TableSpeedSearch(uiTable)

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
            val startOffset = rossyntService.convertToVirtualOffset(textRange.startOffset)
            val endOffset = rossyntService.convertToVirtualOffset(textRange.endOffset)
            if (startOffset != null && endOffset != null) {
                myRangeHighlighter = FileEditorManager.getInstance(project).selectedTextEditor?.markupModel?.addRangeHighlighter(
                    EditorColors.SEARCH_RESULT_ATTRIBUTES, startOffset, endOffset, HighlighterLayer.SELECTION - 2,
                    HighlighterTargetArea.EXACT_RANGE
                )
            }
        }
    }

    private fun removeRangeHighlighters() {
        myRangeHighlighter?.dispose()
        myRangeHighlighter = null
    }
}
