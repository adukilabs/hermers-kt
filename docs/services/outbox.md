# Offline Outbox & Retry Engine

The Hermes Android SDK guarantees zero message loss across unexpected process termination, device crashes, and offline flights.

---

## 1. Atomic Journaling

Whenever a user mutates state (sends an email, moves a message, marks an email read):
1. The local entity is updated optimistically with `dirty = true`.
2. An `Outbox` entity is inserted into ObjectBox inside the **same ACID transaction**.

If the transaction fails, both the local state update and the outbox mutation roll back together.

---

## 2. Background Worker with Decorrelated Jitter

The background `Worker` consumes pending mutations sequentially:
- Reads oldest pending actions first.
- Invokes network transport.
- On network error: increments `attempts` and applies **Decorrelated Jitter** backoff delay.
- Halts subsequent mutations until the current mutation succeeds to prevent out-of-order race conditions.
- On network success: removes the outbox record and clears `dirty = false` on the target message.

---

## 3. Manual Flushing

```kotlin
viewModelScope.launch {
    val dispatchedCount = hermes.sync.flush()
    Log.d("Hermes", "Flushed $dispatchedCount pending actions")
}
```

Check count of pending mutations:

```kotlin
val pendingCount = hermes.sync.pending()
```
