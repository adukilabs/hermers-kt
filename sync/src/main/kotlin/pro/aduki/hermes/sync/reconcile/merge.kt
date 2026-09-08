package pro.aduki.hermes.sync.reconcile

import pro.aduki.hermes.store.entities.Message

/**
 * Reconcile merges server message updates with local dirty states.
 */
object Reconcile {

    /**
     * Merges server flags with local state:
     * If the message is locally marked dirty, local flags are preserved until pending outbox commits.
     * Otherwise, server flags take precedence.
     */
    fun mergeFlags(local: Message, serverFlags: Int): Int {
        return if (local.dirty) {
            local.flags
        } else {
            serverFlags
        }
    }
}

