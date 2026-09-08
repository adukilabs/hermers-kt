# Mail Service

The `hermes.mail` service provides high-level APIs for email management, including sending, reading, flag updates, and moving messages.

---

## 1. Sending Email (Optimistic Offline First)

```kotlin
viewModelScope.launch {
    val message = hermes.mail.send(
        to = listOf("partner@example.com"),
        subject = "Quarterly Review",
        body = "Please review the attached quarterly numbers.",
        mailbox = "outbox"
    )

    Log.d("Hermes", "Message enqueued with hex: ${message.hex}")
}
```

The message is saved immediately to the local ObjectBox database with `dirty = true` and committed to the `Outbox` journal. The background worker flushes the outbox in the background.

---

## 2. Flagging & Toggling Read/Starred

```kotlin
// Mark as Starred
hermes.mail.flag(messageHex, Message.FLAGGED)

// Toggle Read / Unread
hermes.mail.flag(messageHex, Message.SEEN)
```

The flag bitmask is updated in local memory and database instantly. A `"flag"` mutation is queued for background dispatch.

---

## 3. Moving & Removing Messages

```kotlin
// Move to Archive
hermes.mail.move(messageHex, "archive")

// Move to Trash / Delete
hermes.mail.remove(messageHex)
```

---

## 4. Reactive StateFlow Observation

```kotlin
// Observe all messages in Inbox sorted chronologically
val inboxMessages: StateFlow<List<Message>>? = hermes.mail.observe("inbox")

// Observe all known mailboxes
val allMailboxes: StateFlow<List<Mailbox>>? = hermes.mail.mailboxes()

// Observe unread count for Inbox badge counter
val unreadCount: StateFlow<Int>? = hermes.mail.unread("inbox")
```
