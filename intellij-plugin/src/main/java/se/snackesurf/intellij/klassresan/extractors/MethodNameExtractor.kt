package se.snackesurf.intellij.klassresan.extractors;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.util.regex.Pattern;


class MethodNameExtractor(private val project: Project) {

    fun extractMethodName(file: VirtualFile, line: Int): String {
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(file) ?: return UNKNOWN
        val doc: Document = FileDocumentManager.getInstance().getDocument(file) ?: return UNKNOWN

        val safeLine = line.coerceIn(0, doc.lineCount - 1)
        val offset = doc.getLineStartOffset(safeLine)
        val element = psiFile.findElementAt(offset) ?: return UNKNOWN

        val method = findContainingMethod(element)
        return method?.let { extractMethodNameFromElement(it) } ?: UNKNOWN
    }

    private fun extractMethodNameFromElement(methodElement: PsiElement): String =
        try {
            methodElement.text?.takeIf { it.isNotEmpty() }?.let { extractMethodNameFromText(it) } ?: UNKNOWN
        } catch (e: Exception) {
            println("extractMethodName error: ${e.message}")
            UNKNOWN
        }

    private fun extractMethodNameFromText(text: String): String {
        val noLineComments = text.replace(Regex("//.*"), "")
        val noComments = noLineComments.replace(Regex("(?s)/\\*.*?\\*/"), "")

        return noComments.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("@") }.firstNotNullOfOrNull { line ->
                val matcher = Pattern.compile("(\\w+)\\s*\\(").matcher(line)
                if (matcher.find()) matcher.group(1) else null
            } ?: UNKNOWN
    }

    private fun findContainingMethod(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            val str = current.toString()
            if (str == "FUN" || str.startsWith("PsiMethod")) return current
            current = current.parent
        }
        return null
    }

    companion object {
        private const val UNKNOWN = "unknown_method"
    }
}
