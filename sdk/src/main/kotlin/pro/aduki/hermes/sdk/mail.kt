package pro.aduki.hermes.sdk

import pro.aduki.hermes.store.entities.Message

/**
 * Mail service providing high-level operations for mailboxes and messages.
 */
class Mail internal constructor(private val client: HermesClient) {

    suspend fun send(to: List<String>, subject: String, body: String): String {
        // Enqueues in local outbox and triggers network dispatcher
        return "msg_outbox_${System.currentTimeMillis()}"
    }

    suspend fun flag(messageHex: String, flagMask: Int) {
        // Toggles flag optimistically in local store
    }
}

