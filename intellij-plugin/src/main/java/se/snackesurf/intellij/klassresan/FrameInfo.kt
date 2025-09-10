package se.snackesurf.intellij.klassresan

data class FrameInfo(
    val fileName: String? = "",
    val filePath: String? = "",
    val clazz: String? = "",
    val pkg: String? = "",
    val method: String? = "",
    val line: Int? = -1,
    val offset: Int? = -1,
)

fun FrameInfo.toJson(source: String): String {
    return """
    {
        "fileName":"$fileName",
        "filePath":"$filePath",
        "clazz":"$clazz",
        "pkg":"$pkg",
        "method":"$method",
        "line":$line,
        "offset":$offset,
        "source":"$source"
    }
    """.trimIndent()
}