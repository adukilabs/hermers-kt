# HermesClient API Reference

`HermesClient` is the central facade entry point for all mobile operations in the Hermes Android Kotlin SDK.

---

## 1. Class Signature & Properties

```kotlin
package pro.aduki.hermes.sdk

class HermesClient internal constructor(
    val apiKey: String = "",
    val token: String = "",
    val options: Options,
    val session: Session = Session(),
    val lifecycle: Lifecycle = Lifecycle(),
    private val httpClient: OkHttpClient? = null,
    manager: Manager? = null,
    worker: Worker? = null,
    mailRepo: MailRepo? = null,
    contactRepo: ContactRepo? = null,
    mailboxEngine: MailboxEngine? = null,
    contactEngine: ContactEngine? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
)
```

### Public Member Properties

| Property | Type | Access | Description |
| :--- | :--- | :--- | :--- |
| `apiKey` | `String` | `val` | Static API key string (if initialized via API key). |
| `token` | `String` | `val` | Initial JWT access token string (if initialized via token). |
| `options` | `Options` | `val` | Immutable client network and endpoint options. |
| `session` | `Session` | `val` | Reactive state repository for active tokens and resolved user identity. |
| `lifecycle` | `Lifecycle` | `val` | Application foreground/background lifecycle coordinator. |
| `mail` | `Mail` | `val` | Sub-service for message sending, flag updates, moves, and mailbox observations. |
| `contacts` | `Contacts` | `val` | Sub-service for address book synchronization, contact retrieval, and substring search. |
| `sync` | `Sync` | `val` | Sub-service for CONDSTORE/MODSEQ folder delta sync and outbox flushing. |

---

## 2. Factory & Builder Methods

### `HermesClient.Companion.login`
Interactively authenticates against the Hermes REST API with email, password, and optional 6-digit TOTP code.

```kotlin
suspend fun HermesClient.Companion.login(
    email: String,
    password: String,
    totp: String? = null,
    endpoint: String = Endpoints.REST
): HermesClient
```

- **Parameters**:
  - `email`: RFC 5322 user email address.
  - `password`: Cleartext password (zeroized after dispatch).
  - `totp`: Optional 6-digit numeric verification code.
  - `endpoint`: Base REST API URL (defaults to `https://hermers.aduki.pro/v1`).
- **Return Type**: `HermesClient` — Initialized with returned JWT token pair and eagerly resolved user `Identity`.
- **Throws**: `HermesException.Unauthorized` on invalid credentials/TOTP; `HermesException.Network` on connection error.

---

### `HermesClient.Companion.builder`
Creates a fluent `Builder` instance for custom client configuration.

```kotlin
fun HermesClient.Companion.builder(): HermesClient.Builder
```

#### `Builder` Methods

```kotlin
class Builder {
    fun key(key: String): Builder
    fun token(token: String): Builder
    fun endpoint(endpoint: String): Builder
    fun grpc(host: String, port: Int = Endpoints.GRPC_PORT): Builder
    fun secure(enabled: Boolean): Builder
    fun timeout(seconds: Long): Builder
    fun http(client: OkHttpClient): Builder
    fun manager(manager: Manager): Builder
    fun worker(worker: Worker): Builder
    fun mail(repo: MailRepo): Builder
    fun contacts(repo: ContactRepo): Builder
    fun engines(mailbox: MailboxEngine, contact: ContactEngine): Builder
    fun build(): HermesClient
}
```

- **Validation in `build()`**: Enforces that at least one of `apiKey` or `token` is non-blank (`require(apiKey.isNotBlank() || token.isNotBlank())`).

---

## 3. Session & Lifecycle Methods

### `me`
Resolves and returns the authenticated user and tenant identity profile.

```kotlin
suspend fun me(): Identity?
```

- **Return Type**: `Identity?` — User ID hex, tenant hex, owner status, scopes, and tier. Returns cached instance from `session.identity.value` if already resolved; otherwise queries `GET /v1/auth/whoami`.

### `totp`
Confirms or configures two-factor authentication on the active user account.

```kotlin
suspend fun totp(code: String): Boolean
```

- **Parameters**: `code: String` — Exactly 6 numeric digits.
- **Return Type**: `Boolean` — `true` if server confirms verification.
- **Throws**: `IllegalArgumentException` if code is not 6 digits; `HermesException.Unauthorized` if session expired.

### `refresh`
Rotates the active session tokens using the stored refresh token.

```kotlin
suspend fun refresh(): Boolean
```

- **Return Type**: `Boolean` — `true` if tokens were successfully rotated and updated in session state.

### `logout`
Terminates the session remotely and wipes local credentials.

```kotlin
suspend fun logout(): Boolean
```

- **Return Type**: `Boolean` — `true` if server acknowledged revocation (`POST /v1/auth/logout`). Guarantees `session.clear()` executes locally.

### `pause`
Notifies the SDK that the host application entered the background. Suspends background polling.

```kotlin
fun pause()
```

### `resume`
Notifies the SDK that the host application entered the foreground. Resumes background polling and flushes pending outbox mutations.

```kotlin
fun resume()
```

---

## 4. Complete Sub-Services Index

### `client.mail` (`Mail`)
- `suspend fun send(to: List<String>, subject: String, body: String, mailbox: String = "outbox"): Message`
- `suspend fun flag(hex: String, flag: Int)`
- `suspend fun move(hex: String, dest: String)`
- `suspend fun remove(hex: String)`
- `fun observe(mailboxHex: String): StateFlow<List<Message>>?`
- `fun mailboxes(): StateFlow<List<Mailbox>>?`
- `fun unread(mailboxHex: String): StateFlow<Int>?`

### `client.contacts` (`Contacts`)
- `suspend fun sync(tenant: String = ""): Boolean`
- `fun observe(): StateFlow<List<Contact>>?`
- `fun search(query: String): StateFlow<List<Contact>>?`
- `fun get(hex: String): Contact?`

### `client.sync` (`Sync`)
- `suspend fun all(mailboxes: List<String> = listOf("inbox")): Boolean`
- `suspend fun flush(): Int`
- `fun pending(): Int`

