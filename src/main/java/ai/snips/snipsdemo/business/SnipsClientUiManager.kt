package ai.snips.snipsdemo.business


interface SnipsClientUiManager {
    fun onPlatformReady()
    fun onPlatformError()
    fun onPlatformDebug(status: String)
}