package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

/**
 * Sync stores cursor state for delta synchronizers.
 */
@Entity
data class Sync(
    @Id var id: Long = 0,
    @Index var target: String = "", // "contacts", "mailbox_hex"
    var cursor: String = "",
    var timestamp: Long = 0
)
