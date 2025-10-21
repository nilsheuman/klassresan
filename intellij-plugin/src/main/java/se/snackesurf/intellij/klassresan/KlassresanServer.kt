package se.snackesurf.intellij.klassresan

import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.sun.net.httpserver.HttpServer
import se.snackesurf.intellij.klassresan.handlers.OpenFileHandler
import se.snackesurf.intellij.klassresan.handlers.StacktraceHandler
import se.snackesurf.intellij.klassresan.settings.KlassresanSettings
import java.net.InetSocketAddress

class KlassresanServer(private val project: Project, private var port: Int = 8093) {
    private var server: HttpServer? = null
    private val settings = KlassresanSettings.getInstance(project)

    fun start(): String {
        if (server != null) return "Server != null"
        if (!settings.serverEnabled) {
            println("Server not enabled [${project.name}]")
            return "Server not enabled"
        }
        server = HttpServer.create(InetSocketAddress(port), 0)
        server?.createContext("/open", OpenFileHandler(project))
        server?.createContext("/stacktrace", StacktraceHandler(project))
        server?.executor = AppExecutorUtil.getAppExecutorService()
        server?.start()
        println("Klassresan Server started on port $port [${project.name}]")
        return "Server started on port $port"
    }

    fun stop() {
        server?.stop(0)
        server = null
        println("Klassresan server stopped [${project.name}]")
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

