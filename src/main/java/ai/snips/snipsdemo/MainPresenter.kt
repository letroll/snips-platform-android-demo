package ai.snips.snipsdemo

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*


class MainPresenter : ViewModel(), SnipsClientUiManager, ServiceConnection {

    val plateFormState = MutableLiveData<PlateformState>()
    val snipsPlatformClientStatus = MutableLiveData<String>()
    var snipsService: SnipsService? = null
    var serviceBound = false

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

    //region service
    override fun onServiceDisconnected(name: ComponentName?) {
        serviceBound = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serviceBound = true
        service?.let {
            snipsService = (it as SnipsServiceBinder).getService()
        }
    }

    fun unbound(): Boolean {
        return if (serviceBound) {
            serviceBound = false
            true
        } else false
    }

    //endregion
}