package se.snackesurf.intellij.klassresan.hooks

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import se.snackesurf.intellij.klassresan.*
import se.snackesurf.intellij.klassresan.extractors.ClassNameExtractor
import se.snackesurf.intellij.klassresan.extractors.MethodNameExtractor
import se.snackesurf.intellij.klassresan.extractors.PackageNameExtractor

class EditorCaretListener(private val project: Project) : ProjectManagerListener {
    private val publisher: Publisher = HttpPublisher()
    private var lastLine = 0
    private var lastKey = ""

    init {
        val methodNameExtractor = MethodNameExtractor(project)
        val classNameExtractor = ClassNameExtractor(project)
        val packageNameExtractor = PackageNameExtractor(project)

        val editorEventMulticaster = EditorFactory.getInstance().eventMulticaster
        editorEventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
//                println("caretPositionChanged $event")
                val editor = event.editor
                val doc = editor.document
                val vFile = FileDocumentManager.getInstance().getFile(doc) ?: return

                val line = event.newPosition.line
                if (line == lastLine) {
                    return
                }
                lastLine = line

                val packageName = packageNameExtractor.extractPackageName(vFile)
                val className = classNameExtractor.extractClassName(vFile, line)
                val methodName = methodNameExtractor.extractMethodName(vFile, line)

                val key = "$packageName.$className.$methodName"
                if (key == lastKey) {
                    return
                }
                lastKey = key

                val frame = FrameInfo(
                    fileName = vFile.name,
                    filePath = vFile.path,
                    clazz = className,
                    pkg = packageName,
                    line = line + 1,
                    offset = event.editor.caretModel.offset,
                    method = methodName
                )

                publisher.publish(listOf(frame), "editor")
            }
        }, project)
    }
}
