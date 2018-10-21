package ai.snips.snipsdemo

import ai.snips.hermes.*
import ai.snips.platform.SnipsPlatformClient
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.io.File
import java.util.*

object ClientManager : LifecycleObserver {

    val AUDIO_ECHO_REQUEST = 0
    private val TAG = "ClientManager"
    private val FREQUENCY = 16000
    private val CHANNEL = AudioFormat.CHANNEL_IN_MONO
    private val ENCODING = AudioFormat.ENCODING_PCM_16BIT

    private var client: SnipsPlatformClient? = null
    private var snipsClientUiManager: SnipsClientUiManager? = null
    private var recorder: AudioRecord? = null

    @Volatile
    private var continueStreaming = true


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun clientResume() {
        client?.let {
            startStreaming()
            it.resume()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun clientPause() {
        continueStreaming = false
        client?.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clientDisconnect() {
        client?.disconnect()
    }

    fun startMegazordService(snipsClientUiManager: SnipsClientUiManager?) {
        this.snipsClientUiManager = snipsClientUiManager
        if (client == null) {
            // a dir where the assistant models was unziped. it should contain the folders asr dialogue hotword and nlu
            val assistantDir = File(Environment.getExternalStorageDirectory()
                    .toString(), "snips_android_assistant")

            client = SnipsPlatformClient.Builder(assistantDir)
                    .enableDialogue(true) // defaults to true
                    .enableHotword(true) // defaults to true
                    .enableSnipsWatchHtml(true) // defaults to false
                    .enableLogs(true) // defaults to false
                    .withHotwordSensitivity(0.5f) // defaults to 0.5
                    .enableStreaming(true) // defaults to false
                    .enableInjection(true) // defaults to false
                    .build()

            client?.onPlatformReady = fun() {
                this.snipsClientUiManager?.onPlatformReady()
            }

            client?.onPlatformError = fun(_: SnipsPlatformClient.SnipsPlatformError) {
                this.snipsClientUiManager?.onPlatformError()
            }


            client?.onHotwordDetectedListener = fun() {
                Log.d(TAG, "an hotword was detected !")
            }

            client?.onIntentDetectedListener = fun(intentMessage: IntentMessage) {
                Log.d(TAG, "received an intent: $intentMessage")
                // Do your magic here :D

                // For now, lets just use a random sentence to tell the user we understood but don't know what to do

                val answer = IntentHandler.handleIntent(intentMessage)

                client?.endSession(intentMessage.sessionId, answer)
            }

            client?.onListeningStateChangedListener = fun(isListening: Boolean?) {
                Log.d(TAG, "asr listening state: " + isListening!!)
                // Do you magic here :D
            }

            client?.onSessionStartedListener = fun(sessionStartedMessage: SessionStartedMessage) {
                Log.d(TAG, "dialogue session started: $sessionStartedMessage")
            }

            client?.onSessionQueuedListener = fun(sessionQueuedMessage: SessionQueuedMessage) {
                Log.d(TAG, "dialogue session queued: $sessionQueuedMessage")
            }

            client?.onSessionEndedListener = fun(sessionEndedMessage: SessionEndedMessage) {
                Log.d(TAG, "dialogue session ended: $sessionEndedMessage")
            }

            // This api is really for debugging purposes and you should not have features depending on its output
            // If you need us to expose more APIs please do ask !
            client?.onSnipsWatchListener = fun(s: String) {
                this.snipsClientUiManager?.onPlatformDebug(s)
            }

            // We enabled steaming in the builder, so we need to provide the platform an audio stream. If you don't want
            // to manage the audio stream do no enable the option, and the snips platform will grab the mic by itself
            startStreaming()

            client?.connect(DemoApplication.INSTANCE)
        }
    }

    private fun startStreaming() {
        continueStreaming = true
        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            runStreaming()
        }.start()
    }

    private fun runStreaming() {
        Log.d(TAG, "starting audio streaming")
        val minBufferSizeInBytes = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING)
        Log.d(TAG, "minBufferSizeInBytes: $minBufferSizeInBytes")

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, minBufferSizeInBytes)
        recorder?.startRecording()

        while (continueStreaming) {
            val buffer = ShortArray(minBufferSizeInBytes / 2)
            recorder?.read(buffer, 0, buffer.size)
            client?.sendAudioBuffer(buffer)
        }
        recorder?.stop()
        Log.d(TAG, "audio streaming stopped")
    }

    fun startSession() {
        client?.startSession(null, ArrayList(), false, null)
    }

    fun inject(values: HashMap<String, List<String>>) {
        client?.requestInjection(InjectionRequestMessage(
                listOf(InjectionOperation(InjectionKind.Add, values)),
                HashMap()))
    }
}