package ai.snips.snipsdemo


interface SnipsClientUiManager {
    fun onPlatformReady()
    fun onPlatformError()
    fun onPlatformDebug(status: String)
}