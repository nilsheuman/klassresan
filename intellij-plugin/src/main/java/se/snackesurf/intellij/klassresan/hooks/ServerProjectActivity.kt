package se.snackesurf.intellij.klassresan.hooks

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import se.snackesurf.intellij.klassresan.KlassresanServer

class ServerProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val server = KlassresanServer(project)
        server.start()
        project.putUserData(KEY, server) // store reference
    }

    companion object {
        val KEY = com.intellij.openapi.util.Key<KlassresanServer>("klassresan.server")
    }
}
