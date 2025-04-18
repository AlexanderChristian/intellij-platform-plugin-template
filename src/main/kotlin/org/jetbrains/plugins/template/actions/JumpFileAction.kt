package org.jetbrains.plugins.template.actions


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

class JumpToFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val caretOffset = editor.caretModel.offset
        val document = editor.document
        val text = document.text

        // Extract filename using regex
        val regex = """\[(.*?)\]""".toRegex()
        val match = regex.find(text, caretOffset)
        val filename = match?.groups?.get(1)?.value

        if (filename != null) {
            val rootPath = project.basePath ?: run {
                Messages.showErrorDialog("Project base path is not set.", "Error")
                return
            }

            val rootDir = LocalFileSystem.getInstance().findFileByPath(rootPath)
            if (rootDir != null) {
                val file = findFileRecursively(rootDir, filename)
                if (file != null) {
                    FileEditorManager.getInstance(project).openFile(file, true)
                } else {
                    Messages.showErrorDialog("File not found: $filename", "Error")
                }
            } else {
                Messages.showErrorDialog("Root directory not found: $rootPath", "Error")
            }
        } else {
            Messages.showErrorDialog("No filename detected at caret position.", "Error")
        }
    }

    private fun findFileRecursively(directory: VirtualFile, filename: String): VirtualFile? {
        if (!directory.isDirectory) return null

        for (child in directory.children) {
            if (child.name == filename) {
                return child
            }
            if (child.isDirectory) {
                val found = findFileRecursively(child, filename)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }
}