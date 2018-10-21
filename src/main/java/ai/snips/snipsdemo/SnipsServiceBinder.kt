package ai.snips.snipsdemo

import android.os.Binder


class SnipsServiceBinder(private val snipsService: SnipsService) : Binder() {
    fun getService(): SnipsService {
        return snipsService
    }
}