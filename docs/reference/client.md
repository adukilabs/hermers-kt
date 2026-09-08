# HermesClient API Reference

`HermesClient` is the primary entry point for all mobile operations in the Hermes Android SDK.

---

## 1. Class Definition

```kotlin
class HermesClient internal constructor(
    val apiKey: String,
    val token: String,
    val options: Options,
    val session: Session,
    val lifecycle: Lifecycle
) {
    val mail: Mail
    val contacts: Contacts
    val sync: Sync

    suspend fun me(): Identity?
    suspend fun totp(code: String): Boolean
    suspend fun refresh(): Boolean
    suspend fun logout(): Boolean
    fun pause()
    fun resume()
}
```

---

## 2. Methods

### `me(): Identity?`
Resolves and returns the authenticated user and tenant identity (`Identity(user, tenant, owner, scopes, tier)`). Uses local in-memory cache if already resolved.

### `totp(code: String): Boolean`
Confirms or enables 2FA on the user account with a 6-digit verification code. Issues `PATCH /v1/user/totp`.

### `refresh(): Boolean`
Rotates the active JWT access token using the stored refresh token. Returns `true` on success.

### `logout(): Boolean`
Revokes the session on the server via `POST /v1/auth/logout`, clears in-memory tokens, and wipes KeyStore secrets.

### `pause()`
Notifies the SDK that the application has transitioned to the background, suspending network polling.

### `resume()`
Notifies the SDK that the application has returned to the foreground, reactivating polling and initiating an outbox flush.

---

## 3. Sub-Services

### `client.mail`
- `send(to, subject, body, mailbox)`: Enqueues and dispatches an outbound email.
- `flag(hex, flagMask)`: Optimistically toggles flag bitmask.
- `move(hex, destMailbox)`: Relocates message.
- `remove(hex)`: Marks message deleted or removes.
- `observe(mailboxHex)`: Returns `StateFlow<List<Message>>`.
- `mailboxes()`: Returns `StateFlow<List<Mailbox>>`.
- `unread(mailboxHex)`: Returns `StateFlow<Int>`.

### `client.contacts`
- `sync()`: Triggers address book delta sync.
- `observe()`: Returns `StateFlow<List<Contact>>`.
- `search(query)`: Returns `StateFlow<List<Contact>>` filtered by query.
- `get(hex)`: Synchronous lookup by hex.

### `client.sync`
- `all(mailboxes)`: Executes CONDSTORE/MODSEQ sync across specified mailboxes and address book.
- `flush()`: Flushes all queued offline outbox mutations.
- `pending()`: Returns count of pending offline mutations.
