package se.snackesurf.intellij.klassresan

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class EditorCaretListener(private val project: Project) : ProjectManagerListener {
    private val publisher: Publisher = HttpPublisher()

    init {

        val editorEventMulticaster = EditorFactory.getInstance().eventMulticaster
        editorEventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                println("caretPositionChanged $event")
                val editor = event.editor
                val doc = editor.document
                val vFile = FileDocumentManager.getInstance().getFile(doc) ?: return


                val line = event.newPosition.line
                val extractor = MethodNameExtractor(project)

                val methodName = extractor.extractMethodName(vFile, line)
                val frame = FrameInfo(
                    fileName = vFile.name,
                    filePath = vFile.path,
                    line = line + 1,
                    offset = event.editor.caretModel.offset,
                    method = methodName
                )

                publisher.publish(listOf(frame), "editor")
            }
        }, project)
    }
}
