package ai.snips.snipsdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*


class MainPresenter : ViewModel(), SnipsClientUiManager {

    val plateFormState = MutableLiveData<PlateformState>()
    val snipsPlatformClientStatus = MutableLiveData<String>()

    override fun onCleared() {
        super.onCleared()
    }

    override fun onPlatformReady() {
        plateFormState.value = PlateformState.READY
    }

    override fun onPlatformError() {
        plateFormState.value = PlateformState.ERROR
    }

    override fun onPlatformDebug(status: String) {
        snipsPlatformClientStatus.value = status
    }

    fun startSnipSession() {
        ClientManager.startSession()
    }

    fun snipsInjection() {
        val values = HashMap<String, List<String>>()
        values["house_room"] = Arrays.asList("bunker", "batcave")
        ClientManager.inject(values)
    }
}