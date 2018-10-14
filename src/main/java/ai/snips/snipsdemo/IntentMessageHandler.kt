package ai.snips.snipsdemo

import ai.snips.hermes.IntentMessage

interface IntentMessageHandler {

    fun canHandle(intentMessage: IntentMessage): Boolean
    fun handle(intentMessage: IntentMessage): String
}
