package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

/**
 * Message represents an email message stored in ObjectBox FlatBuffers binary format.
 */
@Entity
data class Message(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    @Index var mailbox: String = "",
    @Index var uid: Long = 0,
    var subject: String = "",
    var from: String = "",
    var to: String = "", // Comma-delimited recipients for FlatBuffers efficiency
    var snippet: String = "",
    var blob: String = "",
    var size: Long = 0,
    @Index var flags: Int = 0, // Bitmask: SEEN=1, ANSWERED=2, FLAGGED=4, DELETED=8, DRAFT=16
    @Index var date: Long = 0,
    var created: Long = 0,
    var dirty: Boolean = false
) {
    companion object {
        const val SEEN = 1
        const val ANSWERED = 2
        const val FLAGGED = 4
        const val DELETED = 8
        const val DRAFT = 16
    }

    fun seen(): Boolean = (flags and SEEN) != 0

    fun flagged(): Boolean = (flags and FLAGGED) != 0

    fun toggle(flag: Int) {
        flags = flags xor flag
        dirty = true
    }

    fun recipients(): List<String> {
        if (to.isBlank()) return emptyList()
        return to.split(",").map { it.trim() }
    }
}
