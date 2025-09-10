package se.snackesurf.intellij.klassresan.extractors

import java.awt.Component
import java.awt.Container
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

object HierarchyExtractor {

    /**
     * Collects all paths from leaf nodes to root.
     * Each path is a list of nodes from bottom (leaf) to top (root).
     */
    fun collectPathsFromTree(node: TreeNode): List<List<Any>> {
        val result = mutableListOf<List<Any>>()

        val childrenCount = node.childCount
        if (childrenCount == 0) {
            // Leaf node → path is just this node
            val userObject = (node as? DefaultMutableTreeNode)?.userObject ?: node
            result.add(listOf(userObject))
            return result
        }

        for (i in 0 until childrenCount) {
            val child = node.getChildAt(i)
            val childPaths = collectPathsFromTree(child)
            val userObject = (node as? DefaultMutableTreeNode)?.userObject ?: node
            for (path in childPaths) {
                result.add(path + userObject) // append current node to path (bottom → top)
            }
        }

        return result
    }

    fun parseHierarchyLine(input: String): List<String> {
        // Handle empty input
        if (input.isEmpty()) {
            return listOf("", "", "", "")
        }

        // Extract package name (everything after the last '(' and before the ')')
        val packageName = input.substringAfterLast('(', "").substringBefore(')')

        // Extract everything before the package name
        val beforePackage = input.substringBeforeLast('(', "").substringBeforeLast(')')

        // Extract method name and parameters
        val methodPart = beforePackage.substringAfterLast('.')

        // Extract class name (everything before the first dot)
        val className = if (beforePackage.contains('.')) {
            beforePackage.substringBefore('.')
        } else {
            ""
        }

        // Extract method name (everything before parentheses)
        val methodName = methodPart.substringBefore('(')

        // Extract parameter list (everything between parentheses)
        val parameterList = if (methodPart.contains('(')) {
            methodPart.substringAfter('(', "").substringBefore(')')
        } else {
            ""
        }

        return listOf(
            className,
            methodName,
            parameterList,
            packageName
        )
    }

    fun findHierarchyTree(component: Component): JTree? {
        if (component is JTree) return component
        if (component is Container) {
            for (child in component.components) {
                val result = findHierarchyTree(child)
                if (result != null) return result
            }
        }
        return null
    }
}