package se.snackesurf.intellij.klassresan.extractors

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.util.regex.Pattern

class ClassNameExtractor(private val project: Project) {

    fun extractClassName(file: VirtualFile, line: Int): String {
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(file) ?: return UNKNOWN
        val doc: Document = FileDocumentManager.getInstance().getDocument(file) ?: return UNKNOWN

        val safeLine = line.coerceIn(0, doc.lineCount - 1)
        val offset = doc.getLineStartOffset(safeLine)
        val element = psiFile.findElementAt(offset) ?: return UNKNOWN

        val clazz = findContainingClass(element)
        return clazz?.let { extractClassNameFromElement(it) } ?: UNKNOWN
    }

    private fun extractClassNameFromElement(classElement: PsiElement): String =
        try {
            classElement.text?.takeIf { it.isNotEmpty() }?.let { extractClassNameFromText(it) } ?: UNKNOWN
        } catch (e: Exception) {
            println("extractClassName error: ${e.message}")
            UNKNOWN
        }

    private fun extractClassNameFromText(text: String): String {
        val noLineComments = text.replace(Regex("//.*"), "")
        val noComments = noLineComments.replace(Regex("(?s)/\\*.*?\\*/"), "")

        return noComments.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("@") }
            .firstNotNullOfOrNull { line ->
                val matcher = Pattern.compile("\\b(class|interface|object)\\s+(\\w+)").matcher(line)
                if (matcher.find()) matcher.group(2) else null
            } ?: UNKNOWN
    }

    private fun findContainingClass(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            val str = current.toString()
            if (str.startsWith("PsiClass") || str == "CLASS" || str == "OBJECT") {
                return current
            }
            current = current.parent
        }
        return null
    }

    companion object {
        private const val UNKNOWN = "unknown_class"
    }
}