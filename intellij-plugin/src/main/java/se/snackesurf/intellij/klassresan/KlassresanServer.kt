package se.snackesurf.intellij.klassresan

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.concurrency.AppExecutorUtil
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import se.snackesurf.intellij.klassresan.settings.KlassresanSettings
import java.net.InetSocketAddress

class KlassresanServer(private val project: Project, private var port: Int = 8093) {
    private var server: HttpServer? = null
    private val settings = KlassresanSettings.getInstance()

    fun start(): String {
        if (server != null) return "Server != null"
        if (!settings.serverEnabled) {
            println("Server not enabled")
            return "Server not enabled"
        }
        server = HttpServer.create(InetSocketAddress(port), 0)
        server?.createContext("/open", OpenFileHandler(project))
        server?.executor = AppExecutorUtil.getAppExecutorService()
        server?.start()
        println("Server started on port $port")
        return "Server started on port $port"
    }

    fun stop() {
        server?.stop(0)
        server = null
        println("Klassresan server stopped")
    }

    fun isRunning(): Boolean = server != null
    fun getPort(): Int = port
    fun setPort(newPort: Int) {
        if (port == newPort) return
        stop()
        port = newPort
        start()
    }
}

private class OpenFileHandler(private val project: Project) : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        val params = exchange.requestURI.query.orEmpty()
            .split("&")
            .mapNotNull { it.split("=").takeIf { it.size == 2 }?.let { p -> p[0] to p[1] } }
            .toMap()

        val line = params["line"]?.toIntOrNull() ?: 1

        val found = when {
            params["path"] != null -> openFileByPath(params["path"]!!, line)
            params["fq"] != null -> openFileByFqClass(params["fq"]!!, line)
            else -> false
        }

        sendResponse(exchange, if (found) "OK" else "Not Found")
    }

    private fun openFileByPath(path: String, line: Int): Boolean {
        val vFile = LocalFileSystem.getInstance().findFileByPath(path) ?: return false
        openInEditor(vFile, line)
        return true
    }

    private fun openFileByFqClass(fqName: String, line: Int): Boolean {
        val psiClass = ReadAction.compute<PsiClass?, RuntimeException> {
            JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.allScope(project))
        } ?: return false

        val vFile = psiClass.containingFile?.virtualFile ?: return false
        openInEditor(vFile, line)
        return true
    }

    private fun openInEditor(vFile: com.intellij.openapi.vfs.VirtualFile, line: Int) {
        ApplicationManager.getApplication().invokeLater {
            val editorManager = FileEditorManager.getInstance(project)
            editorManager.openFile(vFile, true)
            val editor = editorManager.selectedTextEditor ?: return@invokeLater
            val document = editor.document
            val safeLine = (line - 1).coerceIn(0, document.lineCount - 1)
            val offset = document.getLineStartOffset(safeLine)
            editor.caretModel.moveToOffset(offset)
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

    private fun sendResponse(exchange: HttpExchange, message: String) {
        val bytes = message.toByteArray()
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
}
