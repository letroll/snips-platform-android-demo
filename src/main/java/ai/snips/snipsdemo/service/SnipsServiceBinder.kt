package ai.snips.snipsdemo.service

import android.os.Binder


class SnipsServiceBinder(private val snipsService: SnipsService) : Binder() {
    fun getService(): SnipsService {
        return snipsService
    }
}