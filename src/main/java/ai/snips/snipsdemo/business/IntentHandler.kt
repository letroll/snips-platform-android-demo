package ai.snips.snipsdemo.business

import ai.snips.hermes.IntentMessage
import ai.snips.snipsdemo.business.handler.WikipediaHandler
import android.util.Log

object IntentHandler {

    private val listOfHandler = arrayListOf<IntentMessageHandler>()

    init {
        listOfHandler.add(WikipediaHandler())
    }

    fun handleIntent(intentMessage: IntentMessage): String {

        Log.d("test", intentMessage.toString())
        listOfHandler.forEach {
            if (it.canHandle(intentMessage)) {
                return it.handle(intentMessage)
            }
        }
        return "on va dire que j'ai compris ${intentMessage.input}"
    }
}