package pro.aduki.hermes.store.queries

import io.objectbox.BoxStore

/**
 * BatchWriter provides atomic ACID transaction execution over ObjectBox.
 */
class BatchWriter(private val store: BoxStore) {

    fun <T> inTx(block: () -> T): T {
        return store.callInTx(block)
    }

    fun runInTx(block: Runnable) {
        store.runInTx(block)
    }
}

