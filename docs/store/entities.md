# FlatBuffers Entity Schemas

Entities are annotated with ObjectBox annotations and compiled into FlatBuffers table descriptors at build time.

---

## 1. Message Entity

Represents an email message stored in native FlatBuffers binary format:

```kotlin
@Entity
data class Message(
    @Id var id: Long = 0,
    @Index var hex: String = "",
    @Index var mailbox: String = "",
    @Index var uid: Long = 0,
    var subject: String = "",
    var from: String = "",
    var to: String = "", // Comma-delimited recipients for zero-copy efficiency
    var snippet: String = "",
    var blob: String = "",
    var size: Long = 0,
    @Index var flags: Int = 0, // Bitmask: SEEN=1, ANSWERED=2, FLAGGED=4, DELETED=8, DRAFT=16
    @Index var date: Long = 0,
    var created: Long = 0,
    var dirty: Boolean = false
)
```

### Flags Bitmask Constants
- `Message.SEEN`: `1`
- `Message.ANSWERED`: `2`
- `Message.FLAGGED`: `4`
- `Message.DELETED`: `8`
- `Message.DRAFT`: `16`

---

## 2. Mailbox Entity

Tracks mailbox metadata and RFC 7162 sequence counters:

```kotlin
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

---

## 3. Contact Entity

Represents an address book contact with search indexes:

```kotlin
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

## 4. Outbox Entity

Stores atomic offline mutations queued for network dispatch:

```kotlin
@Entity
data class Outbox(
    @Id var id: Long = 0,
    @Index var action: String = "", // send, flag, move, delete
    var payload: ByteArray = byteArrayOf(),
    @Index var created: Long = System.currentTimeMillis(),
    var attempts: Int = 0,
    var nextRetry: Long = 0
)
```
