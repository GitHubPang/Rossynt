package org.example.githubpang.rossynt.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.selected
import com.intellij.util.text.nullize
import javax.swing.JPanel

internal class PluginSettingsUi {
    val rootComponent: JPanel
    private val radioButtonDotNetPathAutomatic: JBRadioButton
    private val radioButtonDotNetPathCustom: JBRadioButton
    private val textFieldDotNetPath: TextFieldWithBrowseButton

    // ******************************************************************************** //

    init {
        var radioButtonDotNetPathAutomatic: JBRadioButton? = null
        var radioButtonDotNetPathCustomCell: Cell<JBRadioButton>? = null
        var radioButtonDotNetPathCustom: JBRadioButton? = null
        var textFieldDotNetPath: TextFieldWithBrowseButton? = null

        rootComponent = panel {
            buttonsGroup {
                row {
                    radioButtonDotNetPathAutomatic = radioButton("Automatic").component
                }
                row {
                    radioButtonDotNetPathCustomCell = radioButton("Custom")
                    radioButtonDotNetPathCustom = radioButtonDotNetPathCustomCell!!.component
                }
                indent {
                    row("Path to dotnet executable:") {
                        textFieldDotNetPath = textFieldWithBrowseButton(
                            "Select Executable",
                            fileChooserDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor().withFileFilter {
                                it.name.startsWith("dotnet")
                            },
                        )
                            .enabledIf(radioButtonDotNetPathCustomCell!!.selected)
                            .component
                    }
                }
            }
        }

        this@PluginSettingsUi.radioButtonDotNetPathAutomatic = radioButtonDotNetPathAutomatic ?: throw IllegalStateException()
        this@PluginSettingsUi.radioButtonDotNetPathCustom = radioButtonDotNetPathCustom ?: throw IllegalStateException()
        this@PluginSettingsUi.textFieldDotNetPath = textFieldDotNetPath ?: throw IllegalStateException()
    }

    var dotNetPath: String?
        get() {
            return if (radioButtonDotNetPathAutomatic.isSelected) {
                null
            } else textFieldDotNetPath.text.trim().nullize()
        }
        set(value) {
            radioButtonDotNetPathAutomatic.isSelected = value == null
            radioButtonDotNetPathCustom.isSelected = value != null
            textFieldDotNetPath.text = value ?: ""
        }
}
