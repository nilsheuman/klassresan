package se.snackesurf.intellij.klassresan.extractors

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

class PackageNameExtractor(private val project: Project) {
    fun extractPackageName(vFile: VirtualFile): String {
        val psiFile: PsiFile? = PsiManager.getInstance(project).findFile(vFile)
        val text = psiFile?.text ?: ""
        val packageRegex = Regex("""^\s*package\s+([\w\.]+)""", RegexOption.MULTILINE)
        val packageMatch = packageRegex.find(text)
        return packageMatch?.groupValues?.get(1) ?: ""
    }
}