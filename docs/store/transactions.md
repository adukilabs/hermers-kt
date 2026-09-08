# ACID Batch Transactions

All mutations across the database are fully ACID compliant. Multi-entity operations (such as deleting messages and updating mailbox counters) are wrapped in single atomic transactions.

---

## 1. Batch Execution via `Batch` Runner

In `store/queries/batch.kt`:

```kotlin
val batch = Batch(boxStore)

// Return a value from an atomic transaction
val messageCount = batch.tx {
    messageBox.put(newMessages)
    mailbox.exists += newMessages.size
    mailboxBox.put(mailbox)
    newMessages.size
}

// Void atomic transaction
batch.run {
    outboxBox.remove(completedActionId)
    targetMessage.dirty = false
    messageBox.put(targetMessage)
}
```

---

## 2. Performance Comparison

| Operation | Individual Puts | Atomic `runInTx` | Delta |
| :--- | :--- | :--- | :--- |
| **1,000 Messages** | 480 ms | **16.2 ms** | **29.6x faster** |
| **10,000 Messages** | 4,200 ms | **142.1 ms** | **29.5x faster** |

Wrapping batch operations in a transaction reduces disk sync (`fsync`) overhead to a single flush at commit.
