package pro.aduki.hermes.sync.outbox

import io.objectbox.BoxStore
import pro.aduki.hermes.store.entities.Message
import pro.aduki.hermes.store.entities.Outbox

/**
 * Storage abstracts outbox persistence operations for ObjectBox and test harnesses.
 */
interface Storage {
    fun getMessage(hex: String): Message?
    fun putMessage(msg: Message)
    fun getOutbox(id: Long): Outbox?
    fun putOutbox(entry: Outbox): Long
    fun removeOutbox(id: Long)
    fun pending(): List<Outbox>
    fun <T> tx(block: () -> T): T
}

/**
 * Manager coordinates optimistic local modifications with persistent outbox journaling.
 */
class Manager(private val storage: Storage) {

    constructor(store: BoxStore) : this(object : Storage {
        private val messages = store.boxFor(Message::class.java)
        private val outbox = store.boxFor(Outbox::class.java)

        override fun getMessage(hex: String): Message? = messages.all.firstOrNull { it.hex == hex }
        override fun putMessage(msg: Message) { messages.put(msg) }
        override fun getOutbox(id: Long): Outbox? = outbox.get(id)
        override fun putOutbox(entry: Outbox): Long = outbox.put(entry)
        override fun removeOutbox(id: Long) { outbox.remove(id) }
        override fun pending(): List<Outbox> = outbox.all.sortedBy { it.created }
        override fun <T> tx(block: () -> T): T = store.callInTx(block)
    })

    /**
     * Optimistically toggles a flag bitmask on a message and journals an outbox action.
     */
    fun flag(hex: String, flag: Int): Outbox {
        return storage.tx {
            val msg = storage.getMessage(hex)
                ?: throw IllegalArgumentException("Message not found: $hex")

            msg.flags = msg.flags xor flag
            msg.dirty = true
            storage.putMessage(msg)

            val payload = "$hex:$flag".toByteArray(Charsets.UTF_8)
            val entry = Outbox(
                action = "flag",
                payload = payload,
                created = System.currentTimeMillis()
            )
            val id = storage.putOutbox(entry)
            entry.copy(id = id)
        }
    }

    /**
     * Optimistically moves a message to a destination mailbox and journals an outbox action.
     */
    fun move(hex: String, dest: String): Outbox {
        return storage.tx {
            val msg = storage.getMessage(hex)
                ?: throw IllegalArgumentException("Message not found: $hex")

            msg.mailbox = dest
            msg.dirty = true
            storage.putMessage(msg)

            val payload = "$hex:$dest".toByteArray(Charsets.UTF_8)
            val entry = Outbox(
                action = "move",
                payload = payload,
                created = System.currentTimeMillis()
            )
            val id = storage.putOutbox(entry)
            entry.copy(id = id)
        }
    }

    /**
     * Optimistically commits an outbound message and journals a send action.
     */
    fun send(msg: Message, raw: ByteArray): Outbox {
        return storage.tx {
            msg.dirty = true
            storage.putMessage(msg)

            val entry = Outbox(
                action = "send",
                payload = raw,
                created = System.currentTimeMillis()
            )
            val id = storage.putOutbox(entry)
            entry.copy(id = id)
        }
    }

    /**
     * Optimistically removes or flags a message as deleted and journals an outbox action.
     */
    fun remove(hex: String): Outbox {
        return storage.tx {
            val msg = storage.getMessage(hex)
            if (msg != null) {
                msg.flags = msg.flags or Message.DELETED
                msg.dirty = true
                storage.putMessage(msg)
            }

            val payload = hex.toByteArray(Charsets.UTF_8)
            val entry = Outbox(
                action = "delete",
                payload = payload,
                created = System.currentTimeMillis()
            )
            val id = storage.putOutbox(entry)
            entry.copy(id = id)
        }
    }

    /**
     * Manually enqueues an outbox action.
     */
    fun enqueue(action: String, payload: ByteArray): Outbox {
        val entry = Outbox(
            action = action,
            payload = payload,
            created = System.currentTimeMillis()
        )
        val id = storage.putOutbox(entry)
        return entry.copy(id = id)
    }

    /**
     * Retrieves all pending outbox actions ordered by creation time.
     */
    fun pending(): List<Outbox> {
        return storage.pending()
    }

    /**
     * Marks an outbox action completed: removes from outbox and clears message dirty state.
     */
    fun complete(id: Long, hex: String? = null) {
        storage.tx {
            storage.removeOutbox(id)
            if (hex != null) {
                val msg = storage.getMessage(hex)
                if (msg != null) {
                    msg.dirty = false
                    storage.putMessage(msg)
                }
            }
        }
    }

    /**
     * Records a failed dispatch attempt, scheduling the next retry.
     */
    fun fail(id: Long, delay: Long) {
        val entry = storage.getOutbox(id) ?: return
        entry.attempts += 1
        entry.nextRetry = System.currentTimeMillis() + delay
        storage.putOutbox(entry)
    }
}
