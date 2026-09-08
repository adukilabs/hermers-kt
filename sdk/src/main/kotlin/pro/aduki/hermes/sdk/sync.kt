package pro.aduki.hermes.sdk

/**
 * Sync service coordinating CONDSTORE/MODSEQ mail sync and address book delta sync.
 */
class Sync internal constructor(private val client: HermesClient) {

    suspend fun syncAll() {
        // Runs incremental synchronization for all registered mailboxes and contacts
    }

    suspend fun flushOutbox() {
        // Flushes pending offline mutations
    }
}

