package se.snackesurf.intellij.klassresan.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "KlassresanSettings", storages = [Storage("klassresan.xml")])
@Service
class KlassresanSettings : PersistentStateComponent<KlassresanSettings.State> {

    data class State(
        var httpBaseUrl: String = "http://localhost:8091",
        var clientEnabled: Boolean = true,
        var serverEnabled: Boolean = true,
        var serverPort: Int = 8093
    )

    private var myState = State()

    override fun getState(): State = myState
    override fun loadState(state: State) {
        myState = state
    }

    var httpBaseUrl: String
        get() = myState.httpBaseUrl
        set(value) { myState.httpBaseUrl = value }

    var clientEnabled: Boolean
        get() = myState.clientEnabled
        set(value) { myState.clientEnabled = value }

    var serverEnabled: Boolean
        get() = myState.serverEnabled
        set(value) { myState.serverEnabled = value }

    var serverPort: Int
        get() = myState.serverPort
        set(value) { myState.serverPort = value }

    companion object {
        fun getInstance(): KlassresanSettings =
            com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(KlassresanSettings::class.java)
    }
}