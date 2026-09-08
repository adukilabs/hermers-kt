package pro.aduki.hermes.sync.outbox

import pro.aduki.hermes.core.retry.Jitter
import pro.aduki.hermes.store.entities.Outbox

/**
 * Dispatcher sends outbox actions over the network.
 */
fun interface Dispatcher {
    suspend fun dispatch(action: Outbox)
}

/**
 * Worker sequentially consumes and dispatches pending outbox actions with Decorrelated Jitter backoff.
 */
class Worker(
    private val manager: Manager,
    private val dispatcher: Dispatcher,
    private val jitter: Jitter = Jitter()
) {

    /**
     * Drains all pending actions that are eligible for dispatch.
     * Returns the count of successfully dispatched actions.
     */
    suspend fun drain(): Int {
        val now = System.currentTimeMillis()
        val pending = manager.pending().filter { it.nextRetry <= now }
        var successCount = 0

        for (action in pending) {
            val ok = process(action)
            if (ok) {
                successCount++
            } else {
                // Break on first failure to maintain sequential action ordering
                break
            }
        }
        return successCount
    }

    /**
     * Processes a single outbox action with backoff on failure.
     */
    suspend fun process(action: Outbox): Boolean {
        return try {
            dispatcher.dispatch(action)
            val hex = extractHex(action)
            manager.complete(action.id, hex)
            true
        } catch (_: Exception) {
            val delay = jitter.next(action.attempts)
            manager.fail(action.id, delay)
            false
        }
    }

    private fun extractHex(action: Outbox): String? {
        return try {
            val str = String(action.payload, Charsets.UTF_8)
            when (action.action) {
                "flag", "move" -> str.substringBefore(":")
                "delete" -> str
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

