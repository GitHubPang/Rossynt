package org.jetbrains.plugins.template.toolWindow

import com.intellij.ui.treeStructure.Tree
import com.intellij.util.messages.MessageBus
import org.jetbrains.plugins.template.CurrentFileNameChangeNotifier
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class MyToolWindow(messageBus: MessageBus) {
    private var currentFilePath: String? = null
    private var tree: Tree? = null
    private var labelStatusMessage: JLabel? = null
    private var buttonRefreshToolWindow: JButton? = null
    private var buttonHideToolWindow: JButton? = null
    var content: JPanel? = null

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

        buttonRefreshToolWindow!!.addActionListener { e: ActionEvent? -> uiUpdateAll() }
        uiUpdateAll()
    }

    private fun uiUpdateAll() {
        val currentDate = Date()
        labelStatusMessage!!.text = """$currentDate $currentFilePath"""
//        labelStatusMessage!!.icon = ImageIcon(javaClass.getResource("/toolWindow/Time-icon.png"))
    }
}
