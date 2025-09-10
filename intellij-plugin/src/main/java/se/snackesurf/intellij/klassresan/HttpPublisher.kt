package se.snackesurf.intellij.klassresan;
import com.intellij.openapi.application.ApplicationManager
import java.net.HttpURLConnection
import java.net.URI

class HttpPublisher : Publisher {

    override fun publish(frames: List<FrameInfo>, source: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val json = toJson(frames, source)

                val uri = URI(BASE_URL + sourceToEndpoint(source))
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

    companion object {
        private const val BASE_URL = "http://localhost:8091"
    }
}
