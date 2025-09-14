package se.snackesurf.intellij.klassresan;

import com.intellij.openapi.application.ApplicationManager
import se.snackesurf.intellij.klassresan.settings.KlassresanSettings
import java.net.HttpURLConnection
import java.net.URI

class HttpPublisher : Publisher {
    private val settings = KlassresanSettings.getInstance()

    override fun publish(frames: List<FrameInfo>, source: String) {
        val baseUrl = settings.httpBaseUrl
        val enabled = settings.clientEnabled
        if (!enabled) {
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val json = toJson(frames, source)

                val uri = URI(baseUrl + sourceToEndpoint(source))
//                println("post $uri")
                val conn = (uri.toURL().openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                conn.outputStream.use { os ->
                    os.write(json.toByteArray())
                }

                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toJson(frames: List<FrameInfo>, source: String): String =
        frames.joinToString(prefix = "[", postfix = "]") { it.toJson(source) }

    private fun sourceToEndpoint(source: String): String {
        return when (source) {
            "debugger" -> "/debugger"
            "editor" -> "/editor"
            "hierarchy" -> "/hierarchy"
            else -> "/unknown"
        }
    }
}
