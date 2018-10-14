package ai.snips.snipsdemo

import ai.snips.snipsdemo.ClientManager.AUDIO_ECHO_REQUEST
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun ensurePermissions(activity: Activity): Boolean {
    val status = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
    if (status != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE), AUDIO_ECHO_REQUEST)
        return false
    }
    return true
}
