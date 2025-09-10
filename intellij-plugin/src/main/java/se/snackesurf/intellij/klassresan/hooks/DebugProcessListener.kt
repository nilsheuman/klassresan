package se.snackesurf.intellij.klassresan.hooks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import se.snackesurf.intellij.klassresan.*
import se.snackesurf.intellij.klassresan.extractors.ClassNameExtractor
import se.snackesurf.intellij.klassresan.extractors.MethodNameExtractor
import se.snackesurf.intellij.klassresan.extractors.PackageNameExtractor

class DebugProcessListener(project: Project) : XDebuggerManagerListener {
  private val methodNameExtractor = MethodNameExtractor(project)
  private val classNameExtractor = ClassNameExtractor(project)
  private val packageNameExtractor = PackageNameExtractor(project)
  private val publisher: Publisher = HttpPublisher()

  override fun processStarted(debugProcess: XDebugProcess) {
    println("processStarted")

    cache.clear()

    val debugSession = debugProcess.session
    debugSession.addSessionListener(object : XDebugSessionListener {
      override fun sessionPaused() {
        println("sessionPaused")
        handleSession(debugSession)
      }
    })
  }

  private fun handleSession(session: XDebugSession) {
    val suspendContext = session.suspendContext ?: return
    val stack = suspendContext.activeExecutionStack ?: return
    val frames = mutableListOf<FrameInfo>()

    stack.computeStackFrames(0, object : XExecutionStack.XStackFrameContainer {
      override fun errorOccurred(error: @NlsContexts.DialogMessage String) {
        // println("computeStackFrames error: $error")
      }

      override fun addStackFrames(stackFrames: List<XStackFrame>, last: Boolean) {
        for (frame in stackFrames) {
          val pos = frame.sourcePosition ?: continue
          val filePath = pos.file.path
          val fileName = pos.file.name
          val line = pos.line + 1
          val offset = pos.offset

          getOrCreateFrameInfo(pos, fileName, filePath, line, offset)?.let { frames.add(it) }
        }

        if (last && frames.isNotEmpty()) {
          publisher.publish(frames, "debugger")
        }
      }
    })
  }

  private fun getOrCreateFrameInfo(pos: XSourcePosition, fileName: String, filePath: String, line: Int, offset: Int): FrameInfo? {
    val key = "$fileName:$line"
    return cache[key] ?: run {
      val method = methodNameExtractor.extractMethodName(pos.file, line - 1)
      val clazz = classNameExtractor.extractClassName(pos.file, line - 1)
      val packageName = packageNameExtractor.extractPackageName(pos.file)
      FrameInfo(fileName = fileName, filePath = filePath, clazz = clazz, pkg = packageName, method = method, line = line, offset = offset).also { cache[key] = it }
    }
  }

  companion object {
    private val cache = mutableMapOf<String, FrameInfo>()
  }
}
