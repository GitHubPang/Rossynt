package org.example.githubpang.rossynt.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.layout.panel
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
        var radioButtonDotNetPathCustom: JBRadioButton? = null
        var textFieldDotNetPath: TextFieldWithBrowseButton? = null

        rootComponent = panel {
            buttonGroup {
                row {
                    radioButtonDotNetPathAutomatic = radioButton("Automatic").component
                }
                row {
                    radioButtonDotNetPathCustom = radioButton("Custom").component

                    row("Path to dotnet executable:") {
                        textFieldDotNetPath = textFieldWithBrowseButton(
                            "Select Executable",
                            fileChooserDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor().withFileFilter {
                                it.name.startsWith("dotnet")
                            },
                        ).component
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
