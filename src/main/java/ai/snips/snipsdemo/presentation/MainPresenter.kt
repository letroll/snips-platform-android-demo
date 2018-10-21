package ai.snips.snipsdemo.presentation

import ai.snips.snipsdemo.business.ClientManager
import ai.snips.snipsdemo.service.SnipsService
import ai.snips.snipsdemo.service.SnipsServiceBinder
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*


class MainPresenter : ViewModel(), ServiceConnection {

    val serviceWorking = MutableLiveData<Boolean>()
    lateinit var snipsService: SnipsService
    var serviceBound = false

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
        serviceWorking.value = serviceBound
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serviceBound = true
        service?.let {
            snipsService = (it as SnipsServiceBinder).getService()
        }
        serviceWorking.value = serviceBound
    }

    fun isServiceUp(): Boolean {
        return serviceBound
    }

    fun unbound(): Boolean {
        return if (serviceBound) {
            serviceBound = false
            true
        } else false
    }

    //endregion
}