package org.example.githubpang.rossynt.listeners

import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import org.example.githubpang.rossynt.CurrentFileNotifier

internal class AppFileEditorManagerListener : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile
        val filePath = if (file != null && file.isValid && file.isInLocalFileSystem) {
            file.path
        } else {
            null
        }

        // Publish message.
        val messageBus = event.manager.project.messageBus
        val publisher = messageBus.syncPublisher(CurrentFileNotifier.TOPIC)
        publisher.currentFileChanged(filePath)
    }
}
