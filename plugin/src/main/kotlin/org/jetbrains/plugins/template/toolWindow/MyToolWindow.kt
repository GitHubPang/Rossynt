package org.jetbrains.plugins.template.toolWindow

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import org.jetbrains.plugins.template.CurrentFileNameChangeNotifier
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

internal class MyToolWindow(messageBus: MessageBus) {
    companion object {
        const val TOOL_WINDOW_ID: String = "Rossynt"  // Must match with toolWindow id in "plugin.xml"
    }

    private var currentFilePath: String? = null
    private var tree: Tree? = null
    private var labelStatusMessage: JLabel? = null
    private var buttonTest: JButton? = null
    var content: JPanel? = null

    // ******************************************************************************** //

    init {
        messageBus.connect().subscribe(CurrentFileNameChangeNotifier.TOPIC, object : CurrentFileNameChangeNotifier {
            override fun currentFileNameChanged(filePath: String?) {
                if (currentFilePath == filePath) {
                    return
                }

                currentFilePath = filePath

                // Update UI.
                uiUpdateAll()
            }
        })

        buttonTest!!.addActionListener {
        }
        uiUpdateAll()
    }

    private fun uiUpdateAll() {
        val currentDate = Date()
        labelStatusMessage!!.text = """$currentDate $currentFilePath"""
//        labelStatusMessage!!.icon = ImageIcon(javaClass.getResource("/toolWindow/Time-icon.png"))

        val model = tree!!.model as DefaultTreeModel
        val root = model.root as DefaultMutableTreeNode
        root.add(DefaultMutableTreeNode("another_child"))
        model.reload(root)
    }
}
