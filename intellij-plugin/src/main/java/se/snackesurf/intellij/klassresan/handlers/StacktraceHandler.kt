package se.snackesurf.intellij.klassresan.handlers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.unscramble.AnalyzeStacktraceUtil
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.apache.http.client.utils.URLEncodedUtils
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class StacktraceHandler(private val project: Project) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val uri = exchange.requestURI
        val params: Map<String, String> = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8)
            .associate { it.name to it.value }

        val stParam = params["st"]
        if (stParam != null) {
            val stacktrace = URLDecoder.decode(stParam, StandardCharsets.UTF_8)
            ApplicationManager.getApplication().invokeLater {
                // This creates a console tab "Stacktrace" with normalized links
                val normalized = normalizeStacktrace(stacktrace)
                AnalyzeStacktraceUtil.addConsole(
                    project,
                    null,
                    "Stacktrace",
                    normalized
                )

            }
            sendResponse(exchange, "OK")
        } else {
            sendResponse(exchange, "Missing st parameter")
        }
    }

    private fun sendResponse(exchange: HttpExchange, message: String) {
        val bytes = message.toByteArray()
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun normalizeStacktrace(raw: String): String {
        return raw
            .replace("\r\n", "\n") // normalize CRLF
            .replace("\r", "\n")
            .replace(Regex("\\s*at\\s+"), "\nat ")  // ensure each 'at ...' starts on a new line
            .trim()
    }

}
