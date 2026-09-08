# ObjectBox FlatBuffers Entity Schemas Reference

All data models in the Hermes Android SDK are compiled as **ObjectBox FlatBuffers tables**, guaranteeing zero-copy memory-mapped reads and sub-millisecond query latencies.

---

## 1. `Message` Entity

Represents an email message stored in native FlatBuffers binary format.

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Message(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    @Index var mailbox: String = "",
    @Index var uid: Long = 0,
    var subject: String = "",
    var from: String = "",
    var to: String = "",
    var snippet: String = "",
    var blob: String = "",
    var size: Long = 0,
    @Index var flags: Int = 0,
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
    fun toggle(flag: Int) { flags = flags xor flag; dirty = true }
    fun recipients(): List<String>
}
```

### Field Definitions

| Field | Type | ObjectBox Annotation | Description |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id` | 64-bit local ObjectBox primary key (auto-incrementing). |
| `hex` | `String` | `@Index` | Globally unique 64-character hexadecimal message ID. |
| `mailbox` | `String` | `@Index` | Mailbox folder identifier (e.g., `"inbox"`, `"archive"`). |
| `uid` | `Long` | `@Index` | Monotonic 64-bit server IMAP UID within the folder. |
| `subject` | `String` | — | Decoded email subject line. |
| `from` | `String` | — | Sender display string (e.g., `Alice <alice@aduki.pro>`). |
| `to` | `String` | — | Comma-delimited recipient list for FlatBuffers array efficiency. |
| `snippet` | `String` | — | First 120 characters of plain text body for inbox previews. |
| `blob` | `String` | — | Relative path or Content-Addressable Storage hash of full raw RFC 822 MIME body. |
| `size` | `Long` | — | Total raw message byte length. |
| `flags` | `Int` | `@Index` | 32-bit integer bitmask of IMAP message flags. |
| `date` | `Long` | `@Index` | Milliseconds timestamp of RFC 2822 date header for list ordering. |
| `created` | `Long` | — | Local insertion milliseconds timestamp. |
| `dirty` | `Boolean` | — | `true` if local modifications have not yet reached upstream server. |

---

## 2. `Mailbox` Entity

Tracks mailbox folder metadata and RFC 7162 CONDSTORE sequence state.

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Mailbox(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    var name: String = "",
    var role: String = "", // inbox, sent, trash, archive, drafts
    var uidnext: Long = 0,
    var uidvalidity: Long = 0,
    var modseq: Long = 0,
    var exists: Int = 0,
    var unseen: Int = 0
)
```

### Field Definitions

| Field | Type | Annotation | Description |
| :--- | :--- | :--- | :--- |
| `id` | `Long` | `@Id` | Local ObjectBox primary key. |
| `hex` | `String` | `@Index` | Unique mailbox folder identifier hex. |
| `name` | `String` | — | User-facing folder display title (e.g. `"Inbox"`, `"Work"`). |
| `role` | `String` | — | Special-use role attribute: `"inbox"`, `"sent"`, `"trash"`, `"archive"`, `"drafts"`. |
| `uidnext` | `Long` | — | Predicted next IMAP UID assigned to incoming mail. |
| `uidvalidity` | `Long` | — | Server mailbox generation token. Mismatches trigger full local re-seed. |
| `modseq` | `Long` | — | Highest 64-bit CONDSTORE sequence counter committed locally. |
| `exists` | `Int` | — | Total message count in the folder. |
| `unseen` | `Int` | — | Unread message count badge. |

---

## 3. `Contact` Entity

Stores address book contacts with memory-mapped search indexes.

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Contact(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    @Index var name: String = "",
    @Index var email: String = "",
    var phone: String = "",
    var company: String = "",
    var vcard: String = "",
    var ctag: String = "",
    var updated: Long = 0
)
```

---

## 4. `Outbox` Entity

Persists pending offline write-ahead actions for reliable background transmission.

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Outbox(
    @Id var id: Long = 0,
    @Index var action: String = "", // send, flag, move, remove
    var payload: ByteArray = byteArrayOf(),
    @Index var created: Long = System.currentTimeMillis(),
    var attempts: Int = 0,
    var nextRetry: Long = 0
)
```

---

## 5. `Sync` Entity

Persists synchronization cursor tokens and generation timestamps.

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Sync(
    @Id var id: Long = 0,
    @Index var target: String = "", // "contacts", "inbox", etc.
    var cursor: String = "",
    var timestamp: Long = 0
)
```

