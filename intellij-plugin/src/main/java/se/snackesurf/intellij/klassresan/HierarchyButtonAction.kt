package se.snackesurf.intellij.klassresan

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import se.snackesurf.intellij.klassresan.extractors.HierarchyExtractor.collectPathsFromTree
import se.snackesurf.intellij.klassresan.extractors.HierarchyExtractor.findHierarchyTree
import se.snackesurf.intellij.klassresan.extractors.HierarchyExtractor.parseHierarchyLine
import javax.swing.tree.TreeNode


class HierarchyButtonAction : AnAction("Klassresan Export") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) ?: return
        val tree = findHierarchyTree(component) ?: return
        val paths = collectPathsFromTree(tree.model.root as TreeNode)

        // send each tree separate to create the proper node-trees
        val publisher = HttpPublisher(project)
        paths.forEach { path ->
            val frames = path.mapIndexed { _, node ->

                val (className, methodName, parameterList, packageName) = parseHierarchyLine(node.toString())
                FrameInfo(
                    clazz = className,
                    pkg = packageName,
                    line = 0,
                    offset = 0,
                    method = methodName
                )
            }.reversed()

            // Send each branch separately
            publisher.publish(frames, "hierarchy")
        }

        println("Published ${paths.size} hierarchy paths")
    }

}
