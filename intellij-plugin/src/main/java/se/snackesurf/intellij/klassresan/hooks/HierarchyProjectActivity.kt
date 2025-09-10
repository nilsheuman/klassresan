package se.snackesurf.intellij.klassresan.hooks

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import se.snackesurf.intellij.klassresan.HierarchyToolbarInjector

class HierarchyProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val connection = project.messageBus.connect()
        connection.subscribe(ToolWindowManagerListener.TOPIC, HierarchyToolbarInjector())
        println("HierarchyToolbarInjector subscribed (ProjectActivity)")
    }
}
