# Sync Service Reference (CONDSTORE / MODSEQ)

The `Sync` service (`client.sync`) orchestrates incremental delta synchronization across email folders and address book contacts using RFC 7162 CONDSTORE / MODSEQ extensions.

---

## 1. Class & Method Signatures

```kotlin
package pro.aduki.hermes.sdk

class Sync internal constructor(...) {
    suspend fun all(mailboxes: List<String> = listOf("inbox")): Boolean

    suspend fun flush(): Int

    fun pending(): Int
}
```

---

## 2. Detailed Method Specifications

### `all`
Executes an incremental delta sync across specified mailbox folders and subsequently syncs contacts for the active tenant.

```kotlin
suspend fun all(mailboxes: List<String> = listOf("inbox")): Boolean
```

#### Parameters

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `mailboxes` | `List<String>` | No | `listOf("inbox")` | List of mailbox hex identifiers or names to synchronize. |

#### Return Value
- **Type**: `Boolean`
- **Description**: Returns `true` if all targeted mailboxes and the address book synced cleanly; `false` if any mailbox delta failed.

---

### `flush`
Immediately drains all queued offline outbox mutations over the active network transport.

```kotlin
suspend fun flush(): Int
```

- **Return Value**: `Int` — Number of outbox mutations successfully processed and drained.
- **Execution**: Invokes `Worker.drain()`. If network is disconnected or the circuit breaker is open, returns early without throwing.

---

### `pending`
Returns the count of unsynced offline mutations currently queued in the local outbox journal.

```kotlin
fun pending(): Int
```

- **Return Value**: `Int` — Count of pending outbox actions (`send`, `flag`, `move`, `remove`).

---

## 3. RFC 7162 CONDSTORE / MODSEQ Protocol Wire Details

### HTTP Request

```http
GET /v1/mailboxes/inbox/changes?modseq=104928 HTTP/1.1
Host: hermers.aduki.pro
Authorization: Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

- **Query Parameters**:
  - `modseq`: The client's highest known 64-bit sequence counter for this mailbox.

### HTTP Response (200 OK)

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8

{
  "mailbox": "inbox",
  "uidvalidity": 1725800000,
  "modseq": 105012,
  "changes": [
    {
      "hex": "msg_01HZ...",
      "uid": 4821,
      "subject": "Deploy Ready",
      "from": "alice@aduki.pro",
      "flags": 1,
      "date": 1725835000000
    }
  ],
  "vanished": [4802, 4805]
}
```

---

## 4. Reconcile & Merge Algorithm

```mermaid
flowchart TD
    Start[Receive Mailbox Changes] --> CheckUID{Server uidvalidity == Local uidvalidity?}
    CheckUID -- No --> Invalidate[Wipe local messages for mailbox & full resync]
    CheckUID -- Yes --> Merge[Reconciliation Transaction]
    Merge --> Purge[Delete vanished UIDs]
    Merge --> Update[Upsert modified messages]
    Update --> CheckDirty{Is local message dirty?}
    CheckDirty -- Yes --> Preserve[Preserve local dirty flags & outbox journal]
    CheckDirty -- No --> Apply[Apply server state directly]
    Apply --> StoreModseq[Commit new mailbox.modseq]
    StoreModseq --> End[Finish Atomic Tx]
```

---

## 5. Data Model: `Sync` Cursor Entity

```kotlin
package pro.aduki.hermes.store.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class Sync(
    @Id var id: Long = 0,
    @Index var target: String = "", // e.g. "contacts", "inbox"
    var cursor: String = "",
    var timestamp: Long = 0
)
```

