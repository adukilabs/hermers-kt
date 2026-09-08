# Sync Engine (CONDSTORE / MODSEQ)

Hermes uses the RFC 7162 **CONDSTORE** and **MODSEQ** protocol extensions. Rather than transferring entire message lists on each poll, the client and server exchange monotonically increasing 64-bit sequence counters.

---

## 1. Sequence Tracking

For each mailbox, two variables track cache state:
- `uidvalidity`: Unique ID identifying the server mailbox generation. If this changes, local messages must be invalidated and re-synced.
- `modseq`: Monotonically increasing sequence number tracking changes since the last sync.

---

## 2. Sync Sequence Flow

```text
Client                                  Server
  │                                       │
  │── MailboxSyncReq(box, uidvalidity, ───▶
  │   known_modseq)                       │
  │                                       │
  │◀── MailboxSyncResp(new, changed, ─────│
  │    removed, modseq, uidvalidity)      │
  ▼                                       ▼
Atomic Transaction:
1. If uidvalidity mismatch → wipe mailbox messages and re-seed
2. Delete removed UIDs
3. Apply changed UIDs (preserving local dirty flags)
4. Insert new UIDs
5. Update mailbox.modseq
```

---

## 3. Triggering Sync

```kotlin
// Sync all mailboxes (defaults to "inbox") and address book
viewModelScope.launch {
    val ok = hermes.sync.all(listOf("inbox", "sent", "archive"))
}
```
