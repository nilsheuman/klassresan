package se.snackesurf.intellij.klassresan.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import se.snackesurf.intellij.klassresan.hooks.ServerProjectActivity
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*

internal class KlassresanConfigurable : Configurable {
    private val mainPanel = JPanel()
    private val serverPanel = JPanel()
    private val logPanel = JPanel()
    private val portField: JTextField = object : JTextField("8093", 6) {
        init {
            preferredSize = Dimension(100, 30)
            maximumSize = Dimension(100, 30)
        }
    }
    private val startButton = JButton("Start Server")
    private val stopButton = JButton("Stop Server")
    private val logArea: JTextArea = object : JTextArea(10, 50) {
        init {
            isEditable = false
        }
    }

    init {
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        serverPanel.layout = BoxLayout(serverPanel, BoxLayout.X_AXIS)
        serverPanel.border = BorderFactory.createTitledBorder("Server")
        serverPanel.add(startButton)
        serverPanel.add(stopButton)
        serverPanel.add(JLabel("Port:"))
        serverPanel.add(portField)

        // Make server panel take full width
        serverPanel.alignmentX = Component.LEFT_ALIGNMENT

        logPanel.layout = BoxLayout(logPanel, BoxLayout.Y_AXIS)
        logPanel.border = BorderFactory.createTitledBorder("Log")
        logPanel.add(JScrollPane(logArea))

        // Make log panel take full width
        logPanel.alignmentX = Component.LEFT_ALIGNMENT

        mainPanel.add(serverPanel)
        mainPanel.add(Box.createVerticalStrut(10))
        mainPanel.add(logPanel)

        // Add a horizontal glue to make server panel stretch
        serverPanel.add(Box.createHorizontalGlue())

        // Add a horizontal glue to make log panel stretch
        logPanel.add(Box.createHorizontalGlue())

        // Set preferred size for panels to ensure they stretch
        serverPanel.preferredSize = Dimension(0, serverPanel.preferredSize.height)
        logPanel.preferredSize = Dimension(0, logPanel.preferredSize.height)

        // Set maximum size to ensure panels stretch properly
        serverPanel.maximumSize = Dimension(Int.MAX_VALUE, serverPanel.preferredSize.height)
        logPanel.maximumSize = Dimension(Int.MAX_VALUE, logPanel.preferredSize.height)

        startButton.addActionListener {
            try {
                val port = portField.text.toInt()
                val project = ProjectManager.getInstance().openProjects[0]
                val server =
                    project.getUserData(ServerProjectActivity.KEY)
                server!!.setPort(port)
                server.start()
                logArea.append("Server started on port $port\n")
            } catch (ex: Exception) {
                logArea.append("Could not start server\n")
            }
        }

        stopButton.addActionListener {
            val project = ProjectManager.getInstance().openProjects[0]
            val server =
                project.getUserData(ServerProjectActivity.KEY)
            server!!.stop()
            logArea.append("Server stopped\n")
        }

        val project1 = ProjectManager.getInstance().openProjects[0]
        val server1 = project1.getUserData(ServerProjectActivity.KEY)
        val status =
            if (server1 != null && server1.isRunning()) "is running on port " + server1.getPort() else "is not running"
        logArea.append("Server $status\n")
    }

    override fun getDisplayName(): String {
        return "Klassresan"
    }

    override fun createComponent(): JComponent {
        return mainPanel
    }

    override fun isModified(): Boolean {
        return true
    }

    override fun apply() {
    }
}