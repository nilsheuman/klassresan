package se.snackesurf.intellij.klassresan

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import java.awt.BorderLayout

class HierarchyToolbarInjector : ToolWindowManagerListener {
    override fun toolWindowsRegistered(ids: List<String>, toolWindowManager: ToolWindowManager) {
//        println("toolWindowsRegistered $ids")
    }
    override fun stateChanged(toolWindowManager: ToolWindowManager) {
//        println("stateChanged")
    }

    override fun toolWindowShown(toolWindow: ToolWindow) {
//        println("toolWindowShown ${toolWindow.id}")
        if (toolWindow.id.endsWith("Hierarchy")) {
            val contentManager = toolWindow.contentManager
            val component = contentManager.component

            // Find toolbar
            val actionManager = ActionManager.getInstance()
            val actionGroup = DefaultActionGroup()
            actionGroup.add(HierarchyButtonAction())

            val toolbar: ActionToolbar = actionManager.createActionToolbar(
                "HierarchyExtraToolbar",
                actionGroup,
                true
            )

            toolbar.targetComponent = component
            component.add(toolbar.component, BorderLayout.EAST)

        }
    }
}
