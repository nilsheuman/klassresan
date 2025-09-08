package se.snackesurf.intellij.klassresan;

data class FrameInfo(
    val fileName: String,
    val filePath: String,
    val line: Int,
    val offset: Int,
    val method: String
)
