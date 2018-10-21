package ai.snips.snipsdemo.business.handler

import ai.snips.hermes.IntentMessage
import ai.snips.snipsdemo.business.IntentMessageHandler

class WikipediaHandler : IntentMessageHandler {
    override fun canHandle(intentMessage: IntentMessage): Boolean {
        return intentMessage.intent.intentName == "Tealque:searchWikipedia"
    }

    override fun handle(intentMessage: IntentMessage): String {
        return "${intentMessage.slots[0].rawValue} est une chose que je ne connait pas"
    }

}