package ai.snips.snipsdemo

import ai.snips.hermes.*
import ai.snips.platform.SnipsPlatformClient
import ai.snips.platform.SnipsPlatformClient.SnipsPlatformError
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class MainActivity : AppCompatActivity() {

    companion object {
        private val AUDIO_ECHO_REQUEST = 0
        private val TAG = "MainActivity"
        private val FREQUENCY = 16000
        private val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    private var client: SnipsPlatformClient? = null
    private var recorder: AudioRecord? = null

    @Volatile
    private var continueStreaming = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ensurePermissions()

        start.setOnClickListener {
            if (ensurePermissions()) {
                it.isEnabled = false
                start.setText(R.string.loading)
                scrollView.visibility = View.GONE
                loadingPanel.visibility = View.VISIBLE
                startMegazordService()
            }
        }
    }

    override fun onDestroy() {
        client?.disconnect()
        super.onDestroy()
    }

    private fun ensurePermissions(): Boolean {
        val status = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO)
        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE), AUDIO_ECHO_REQUEST)
            return false
        }
        return true
    }

    private fun startMegazordService() {
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
                runOnUiThread {
                    loadingPanel.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    start.isEnabled = true
                    start.setText(R.string.start_dialog_session)
                    start.setOnClickListener {
                        // programmatically start a dialogue session
                        client!!.startSession(null, ArrayList(), false, null)
                    }
                    start.setOnLongClickListener {
                        // inject new values in the "house_room" entity
                        val values = HashMap<String, List<String>>()
                        values["house_room"] = Arrays.asList("bunker", "batcave")
                        client!!.requestInjection(InjectionRequestMessage(
                                listOf(InjectionOperation(InjectionKind.Add, values)),
                                HashMap()))

                        true
                    }
                }
            }

            client?.onPlatformError = fun(_: SnipsPlatformError) {
                loadingPanel.visibility = View.VISIBLE
                scrollView.visibility = View.GONE
                start.isEnabled = false
            }


            client?.onHotwordDetectedListener = fun() {
                Log.d(TAG, "an hotword was detected !")
                // Do your magic here :D
            }

            client?.onIntentDetectedListener = fun(intentMessage: IntentMessage) {
                Log.d(TAG, "received an intent: $intentMessage")
                // Do your magic here :D

                // For now, lets just use a random sentence to tell the user we understood but don't know what to do

                val answers = Arrays.asList(
                        "This is only a demo app. I understood you but I don't know how to do that",
                        "Can you teach me how to do that?",
                        "Oops! This action has not be coded yet!",
                        "Yes Master! ... hum, ..., er, ... imagine this as been done",
                        "Let's pretend I've done it! OK?")


                client?.endSession(intentMessage.sessionId, answers[Math.abs(ThreadLocalRandom.current()
                        .nextInt()) % answers
                        .size])
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
                runOnUiThread {
                    // We enabled html logs in the builder, hence the fromHtml. If you only log to the console,
                    // or don't want colors to be displayed, do not enable the option
                    (text as EditText).append(Html.fromHtml("$s<br />"))
                    scrollView.post { (scrollView as ScrollView).fullScroll(View.FOCUS_DOWN) }
                }
            }

            // We enabled steaming in the builder, so we need to provide the platform an audio stream. If you don't want
            // to manage the audio stream do no enable the option, and the snips platform will grab the mic by itself
            startStreaming()

            client?.connect(this.applicationContext)
        }
    }

    private fun startStreaming() {
        continueStreaming = true
        object : Thread() {
            override fun run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                runStreaming()
            }
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

    override fun onResume() {
        super.onResume()
        client?.let {
            startStreaming()
            it.resume()
        }
    }

    override fun onPause() {
        continueStreaming = false
        client?.pause()
        super.onPause()
    }

}
