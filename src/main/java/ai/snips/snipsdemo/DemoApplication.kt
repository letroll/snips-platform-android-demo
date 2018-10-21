package ai.snips.snipsdemo

import ai.snips.snipsdemo.business.PlateformState
import ai.snips.snipsdemo.business.ensurePermissions
import ai.snips.snipsdemo.service.SnipsService
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class DemoApplication : Application() {

    companion object {
        lateinit var INSTANCE: DemoApplication
        const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID_SNIPS "
        const val NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_SNIPS "
    }

    val isSnipsProcess: Boolean
        get() {
            val cmdline = File("/proc/" + android.os.Process.myPid() + "/cmdline")
            try {
                BufferedReader(FileReader(cmdline)).use { reader -> return reader.readLine().contains(":snipsProcessingService") }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }

    override fun onCreate() {
        super.onCreate()

        // Snips launches a second process to run the platform, this allows to completely free the memory used by Snips
        // when not using it (the .so are quite huge, and you can't unload them from a process on Android) and it
        // isolates you app from potential crashes of the platform (we strive no to crash but this can happen, and the
        // OS is less forgiving with a segfault in native code than with an uncaught exception in Java... )
        // The application is instantiated in both the main process and the snips one. If you need to initialize things
        // here, check you're not in the snips process
        if (!isSnipsProcess) {
            Log.i("SnipsDemoApp", "in the main process")
            // do some init here
        } else {
            Log.i("SnipsDemoApp", "in the snips process")
        }

        INSTANCE = this

        createNotificationChannel()
        startSnipsService()
    }

    private fun startSnipsService() {
        if (ensurePermissions(this)) {
            SnipsService.start(this, PlateformState.NOT_READY.ordinal)
        } else {
            SnipsService.start(this, PlateformState.ERROR.ordinal)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}
