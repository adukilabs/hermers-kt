package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

/**
 * Message represents a single email message stored in ObjectBox FlatBuffers binary format.
 */
@Entity
data class Message(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    @Index var mailbox: String = "",
    @Index var uid: Long = 0,
    var subject: String = "",
    var from: String = "",
    var to: String = "", // Comma-delimited recipient list for FlatBuffers efficiency
    var snippet: String = "",
    var blob: String = "",
    var size: Long = 0,
    @Index var flags: Int = 0, // Bitmask: SEEN=1, ANSWERED=2, FLAGGED=4, DELETED=8, DRAFT=16
    @Index var date: Long = 0,
    var created: Long = 0,
    var dirty: Boolean = false
) {
    companion object {
        const val FLAG_SEEN = 1
        const val FLAG_ANSWERED = 2
        const val FLAG_FLAGGED = 4
        const val FLAG_DELETED = 8
        const val FLAG_DRAFT = 16
    }

    fun isSeen(): Boolean = (flags and FLAG_SEEN) != 0
    fun isFlagged(): Boolean = (flags and FLAG_FLAGGED) != 0

    fun toggleFlag(flagMask: Int) {
        flags = flags xor flagMask
        dirty = true
    }
}

