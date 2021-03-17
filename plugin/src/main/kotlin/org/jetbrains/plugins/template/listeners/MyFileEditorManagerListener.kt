package org.jetbrains.plugins.template.listeners

import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import org.jetbrains.plugins.template.CurrentFileNameChangeNotifier

internal class MyFileEditorManagerListener : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile
        val filePath = if (file != null && file.isValid && file.isInLocalFileSystem) {
            file.path
        } else {
            null
        }

        // Publish message.
        val messageBus = event.manager.project.messageBus
        val publisher = messageBus.syncPublisher(CurrentFileNameChangeNotifier.TOPIC)
        publisher.currentFileNameChanged(filePath)
    }
}
