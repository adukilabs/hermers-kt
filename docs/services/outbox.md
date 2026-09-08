# Offline Outbox & Retry Engine Reference

The Hermes Android SDK guarantees zero data loss across network drops, crashes, and device reboots via an atomic write-ahead transactional outbox journal.

---

## 1. Outbox Architecture & Execution Loop

```mermaid
sequenceDiagram
    autonumber
    participant App as Client Call (e.g. send, flag, move)
    participant Store as ObjectBox Store
    participant Worker as Outbox Worker
    participant Server as Hermes REST / gRPC

    App->>Store: Atomic Tx: Write Message + Insert Outbox Journal
    Store-->>App: Return optimistic Message
    App->>Worker: Signal drain()
    activate Worker
    Worker->>Store: Query next pending Outbox action
    Worker->>Server: HTTP POST /v1/messages (X-Hermes-Idempotency-Key: UUID)
    alt Network Failure
        Server-->>Worker: Connection Timeout / 5xx
        Worker->>Store: Increment attempts, calculate Decorrelated Jitter nextRetry
        Worker->>Worker: Suspend until nextRetry
    else Network Success
        Server-->>Worker: 200 OK
        Worker->>Store: Atomic Tx: Delete Outbox record + Clear dirty=false
        Worker->>Store: Query next pending Outbox action
    end
    deactivate Worker
```

---

## 2. Core Method Signatures

### `Manager`

```kotlin
package pro.aduki.hermes.sync.outbox

class Manager(private val box: Box<Outbox>) {
    fun send(msg: Message, raw: ByteArray): Outbox
    fun flag(hex: String, flag: Int): Outbox
    fun move(hex: String, dest: String): Outbox
    fun remove(hex: String): Outbox
    fun pending(): List<Outbox>
    fun remove(id: Long)
}
```

### `Worker`

```kotlin
package pro.aduki.hermes.sync.outbox

class Worker(
    private val manager: Manager,
    private val client: OkHttpClient,
    private val endpoint: String,
    private val circuit: Circuit
) {
    suspend fun drain(): Int
}
```

---

## 3. Data Model: `Outbox` Entity

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Outbox(
    @Id var id: Long = 0,
    @Index var action: String = "", // "send", "flag", "move", "remove"
    var payload: ByteArray = byteArrayOf(),
    @Index var created: Long = System.currentTimeMillis(),
    var attempts: Int = 0,
    var nextRetry: Long = 0
)
```

### Action Types & Binary Payloads

| `action` | Target Entity | Binary Payload Format | Wire Endpoint |
| :--- | :--- | :--- | :--- |
| `"send"` | `Message` | Raw RFC 822 MIME byte array (`message/rfc822`) | `POST /v1/messages` |
| `"flag"` | `Message` | UTF-8 JSON: `{"hex": "...", "flag": 4}` | `POST /v1/messages/:hex/flags` |
| `"move"` | `Message` | UTF-8 JSON: `{"hex": "...", "dest": "archive"}` | `POST /v1/messages/:hex/move` |
| `"remove"`| `Message` | UTF-8 JSON: `{"hex": "..."}` | `DELETE /v1/messages/:hex` |

---

## 4. Idempotency & Replay Protection

To prevent duplicate message dispatches during transient network reconnections, every request carries a deterministic idempotency header derived from the outbox record ID and creation timestamp:

```http
POST /v1/messages HTTP/1.1
Host: hermers.aduki.pro
Authorization: Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...
X-Hermes-Idempotency-Key: 7b9f8a02-1c3d-4e5f-8a9b-0c1d2e3f4a5b
Content-Type: message/rfc822

<raw binary email blob>
```

---

## 5. Decorrelated Jitter Retry Backoff

When transient errors occur, retry delays are computed using Amazon's **Decorrelated Jitter** algorithm to avoid thundering-herd synchronization across mobile fleets:

$$t_{i+1} = \min(t_{\max}, \text{random}(t_{\min}, 3 \times t_i))$$

```kotlin
package pro.aduki.hermes.core.retry

object Jitter {
    fun nextDelay(currentDelayMs: Long, baseMs: Long = 1000L, maxMs: Long = 60000L): Long {
        val high = (currentDelayMs * 3).coerceAtLeast(baseMs)
        val jittered = kotlin.random.Random.nextLong(baseMs, high + 1)
        return jittered.coerceAtMost(maxMs)
    }
}
```

