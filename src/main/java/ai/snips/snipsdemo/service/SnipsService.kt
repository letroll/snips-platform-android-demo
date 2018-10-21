package ai.snips.snipsdemo.service

import ai.snips.snipsdemo.DemoApplication.Companion.NOTIFICATION_CHANNEL_ID
import ai.snips.snipsdemo.R
import ai.snips.snipsdemo.business.ClientManager
import ai.snips.snipsdemo.business.PlateformState
import ai.snips.snipsdemo.business.SnipsClientUiManager
import ai.snips.snipsdemo.presentation.MainActivity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

class SnipsService : Service(), SnipsClientUiManager {

    companion object {
        const val STATE = "STATE"

        fun start(context: Context, state: Int?) {
            val serviceIntent = Intent(context, SnipsService::class.java)
            state?.let {
                serviceIntent.putExtra(STATE, state)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        fun stop(context: Context) {
            val serviceIntent = Intent(context, SnipsService::class.java)
            context.stopService(serviceIntent)
        }

        fun intent(context: Context) = Intent(context, SnipsService::class.java)

    }

    lateinit var notification: Notification
    var plateformState = PlateformState.NOT_READY
    val snipsPlatformClientStatus = MutableLiveData<String>()
    val plateFormState = MutableLiveData<PlateformState>()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return SnipsServiceBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        plateformState = PlateformState.values()[intent?.extras?.getInt(STATE, 0) ?: 0]
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(plateformState.name)
                .setSmallIcon(R.drawable.ic_mic_black_24dp)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(1, notification)

        when (plateformState) {
            PlateformState.NOT_READY -> {
                ClientManager.startMegazordService(this)
            }
            PlateformState.LOADING -> {
            }
            PlateformState.READY -> {
            }
            PlateformState.ERROR -> {
            }
        }

        return START_NOT_STICKY
    }

    //region snips ui callback
    override fun onPlatformReady() {
        plateFormState.value = PlateformState.READY
        start(baseContext, PlateformState.READY.ordinal)
    }

    override fun onPlatformError() {
        plateFormState.value = PlateformState.ERROR
        start(baseContext, PlateformState.ERROR.ordinal)
    }

    override fun onPlatformDebug(status: String) {
        snipsPlatformClientStatus.value = status
    }
    //endregion
}
