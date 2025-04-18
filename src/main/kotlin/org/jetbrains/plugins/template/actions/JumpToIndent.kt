package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.TextRange


class SelectIndentedBlockAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val caret = editor.caretModel
        val selectionModel = editor.selectionModel

        val startLine = caret.logicalPosition.line
        val startOffset = document.getLineStartOffset(startLine)
        var endLine = startLine + 1

        val startLineRange = TextRange(document.getLineStartOffset(startLine), document.getLineEndOffset(startLine))
        val startLineText = document.getText(startLineRange)
        val currentIndent = startLineText.takeWhile { ch -> ch == ' ' || ch == '\t' }

        while (endLine < document.lineCount) {
            val lineStartOffset = document.getLineStartOffset(endLine)
            val lineEndOffset = document.getLineEndOffset(endLine)
            val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
            val lineIndent = lineText.takeWhile { ch -> ch == ' ' || ch == '\t' }

            if (lineText.isNotBlank() && lineIndent.length <= currentIndent.length) {
                break
            }

            endLine++
        }

        val endOffset = document.getLineEndOffset(endLine - 1)
        selectionModel.setSelection(startOffset, endOffset)
    }
}