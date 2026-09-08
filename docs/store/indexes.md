# B-Tree Indexes & Fast Queries

ObjectBox creates native C++ B-Tree indexes directly inside memory-mapped files. Lookups execute in sub-millisecond timeframes without loading unrequested rows into JVM memory.

---

## 1. Indexed Fields

- `Message.hex`: O(1) hash-indexed unique lookup.
- `Message.mailbox`: Clustered B-Tree index for mailbox message list generation.
- `Message.uid`: B-Tree index for RFC 7162 CONDSTORE sequence reconciliation.
- `Message.date`: B-Tree index for chronological sorting (newest first).
- `Message.flags`: Integer bitmask index for unread / flagged filtering.
- `Contact.name`: Prefix index for instant incremental search.
- `Contact.email`: Index for recipient autocomplete.

---

## 2. Querying Examples

### Chronological Mailbox Query
```kotlin
val messages = messageBox.query()
    .equal(Message_.mailbox, "inbox")
    .orderDesc(Message_.date)
    .build()
    .find(0, 50) // Paging: first 50 messages
```

### Unread Messages Query
```kotlin
val unread = messageBox.query()
    .equal(Message_.mailbox, "inbox")
    .apply {
        // Bitwise unread check: SEEN bit is NOT set
    }
    .build()
    .find()
```
