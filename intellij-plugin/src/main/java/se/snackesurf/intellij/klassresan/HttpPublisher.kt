package se.snackesurf.intellij.klassresan;
import com.intellij.openapi.application.ApplicationManager
import java.net.HttpURLConnection
import java.net.URI

class HttpPublisher : Publisher {

    override fun publish(frames: List<FrameInfo>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val json = toJson(frames)

                val uri = URI(ENDPOINT)
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

    private fun toJson(frames: List<FrameInfo>): String =
        frames.joinToString(prefix = "[", postfix = "]") { f ->
            """{"fileName":"${f.fileName}","filePath":"${f.filePath}","line":${f.line},"offset":${f.offset},"method":"${f.method}"}"""
        }

    companion object {
        private const val ENDPOINT = "http://localhost:8091/frames"
    }
}
