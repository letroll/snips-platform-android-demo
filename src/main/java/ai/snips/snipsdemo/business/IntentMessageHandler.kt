package ai.snips.snipsdemo.business

import ai.snips.hermes.IntentMessage

interface IntentMessageHandler {

    fun canHandle(intentMessage: IntentMessage): Boolean
    fun handle(intentMessage: IntentMessage): String
}
