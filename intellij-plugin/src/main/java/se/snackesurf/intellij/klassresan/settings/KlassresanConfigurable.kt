package se.snackesurf.intellij.klassresan.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import se.snackesurf.intellij.klassresan.hooks.ServerProjectActivity
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

internal class KlassresanConfigurable(private val project: Project) : Configurable {
    private val mainPanel = JPanel()
    private val serverPanel = JPanel()
    private val clientPanel = JPanel()
    private val logPanel = JPanel()
    private val baseUrlField: JTextField = object : JTextField("<set url>", 6) {
        init {
            preferredSize = Dimension(300, LABEL_HEIGHT)
            maximumSize = Dimension(300, LABEL_HEIGHT)
        }
    }
    private val portField: JTextField = object : JTextField("8093", 6) {
        init {
            preferredSize = Dimension(100, LABEL_HEIGHT)
            maximumSize = Dimension(100, LABEL_HEIGHT)
        }
    }
    private val logArea: JTextArea = object : JTextArea(10, 50) {
        init {
            isEditable = false
        }
    }
    private val clientEnabledCheckBox = JCheckBox("Enable Client")
    private val serverEnabledCheckBox = JCheckBox("Enable Server")

    init {
        val settings = KlassresanSettings.getInstance(project)

        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        clientPanel.layout = BoxLayout(clientPanel, BoxLayout.X_AXIS)
        clientPanel.border = BorderFactory.createTitledBorder("Client")
        clientPanel.add(clientEnabledCheckBox)
        
        // Add fixed width label to maintain spacing
        val clientLabel = JLabel("Base URL:")
        clientLabel.preferredSize = Dimension(100, LABEL_HEIGHT)
        clientLabel.maximumSize = Dimension(100, LABEL_HEIGHT)
        clientEnabledCheckBox.preferredSize = Dimension(150, LABEL_HEIGHT)
        clientEnabledCheckBox.maximumSize = Dimension(150, LABEL_HEIGHT)
        serverEnabledCheckBox.preferredSize = Dimension(150, LABEL_HEIGHT)
        serverEnabledCheckBox.maximumSize = Dimension(150, LABEL_HEIGHT)
        clientPanel.add(clientLabel)
        
        clientPanel.add(baseUrlField)
        clientPanel.alignmentX = Component.LEFT_ALIGNMENT

        serverPanel.layout = BoxLayout(serverPanel, BoxLayout.X_AXIS)
        serverPanel.border = BorderFactory.createTitledBorder("Server")
        serverPanel.add(serverEnabledCheckBox)
        
        // Add fixed width label to maintain spacing
        val portLabel = JLabel("Port:")
        portLabel.preferredSize = Dimension(100, LABEL_HEIGHT)
        portLabel.maximumSize = Dimension(100, LABEL_HEIGHT)
        serverPanel.add(portLabel)
        
        serverPanel.add(portField)
        serverPanel.alignmentX = Component.LEFT_ALIGNMENT

        logPanel.layout = BoxLayout(logPanel, BoxLayout.Y_AXIS)
        logPanel.border = BorderFactory.createTitledBorder("Log")
        logPanel.add(JScrollPane(logArea))
        logPanel.alignmentX = Component.LEFT_ALIGNMENT

        mainPanel.add(clientPanel)
        mainPanel.add(Box.createVerticalStrut(10))
        mainPanel.add(serverPanel)
        mainPanel.add(Box.createVerticalStrut(10))
        mainPanel.add(logPanel)

        // Set preferred size for panels to ensure they stretch
        serverPanel.preferredSize = Dimension(0, serverPanel.preferredSize.height)
        logPanel.preferredSize = Dimension(0, logPanel.preferredSize.height)

        // Set maximum size to ensure panels stretch properly
        serverPanel.maximumSize = Dimension(Int.MAX_VALUE, serverPanel.preferredSize.height)
        logPanel.maximumSize = Dimension(Int.MAX_VALUE, logPanel.preferredSize.height)

        // Add listener to server enable checkbox to start/stop server
        serverEnabledCheckBox.addActionListener {
            val server = project.getUserData(ServerProjectActivity.KEY)
            settings.serverEnabled = serverEnabledCheckBox.isSelected
            
            if (serverEnabledCheckBox.isSelected) {
                if (server?.isRunning() == true) {
                    server.stop()
                }

                try {
                    val port = portField.text.toInt()
                    server!!.setPort(port)
                    val status = server.start()
                    logArea.append(status + "\n")
                } catch (ex: Exception) {
                    logArea.append("Could not start server\n${ex.message}\n")
                }
            } else {
                server!!.stop()
                logArea.append("Server stopped\n")
            }
        }

        baseUrlField.text = settings.httpBaseUrl
        clientEnabledCheckBox.isSelected = settings.clientEnabled
        serverEnabledCheckBox.isSelected = settings.serverEnabled
        portField.text = settings.serverPort.toString()

        val server1 = project.getUserData(ServerProjectActivity.KEY)
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
        val settings = KlassresanSettings.getInstance(project)
        return baseUrlField.text != settings.httpBaseUrl ||
                clientEnabledCheckBox.isSelected != settings.clientEnabled ||
                serverEnabledCheckBox.isSelected != settings.serverEnabled ||
                portField.text != settings.serverPort.toString()
    }

    override fun apply() {
        val settings = KlassresanSettings.getInstance(project = project)
        settings.httpBaseUrl = baseUrlField.text
        settings.clientEnabled = clientEnabledCheckBox.isSelected
        settings.serverEnabled = serverEnabledCheckBox.isSelected
        settings.serverPort = portField.text.toInt()
    }
    
    companion object {
        private const val LABEL_HEIGHT = 30
    }
}