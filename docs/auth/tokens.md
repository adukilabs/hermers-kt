# Token Lifecycle & Session Management Reference

Hermes interactive sessions use rotating short-lived JWTs paired with single-use refresh tokens stored in hardware-backed storage.

---

## 1. Token Refresh Specification

### Method Signature

```kotlin
package pro.aduki.hermes.sdk

suspend fun HermesClient.refresh(): Boolean
```

- **Parameters**: None. The SDK extracts the active refresh token from `session.refresh()`.
- **Return Type**: `Boolean`. Returns `true` if rotation succeeded and local session state was updated; `false` if no refresh token was present or the server rejected the request.
- **Side Effects**:
  - Updates `session.tokens` `StateFlow`.
  - Re-encrypts new tokens in `AndroidKeyStore`.
  - In-memory zeroization of the prior refresh token.

### Network Wire Protocol

```http
POST /v1/auth/refresh HTTP/1.1
Host: hermers.aduki.pro
Content-Type: application/json; charset=utf-8
Accept: application/json

{
  "token": "rt_8f3a02c91b4e5d6f7a8b9c0d1e2f3a4b"
}
```

#### JSON Response (200 OK)

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8

{
  "token": "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh": "rt_901c3d4e5f6a7b8c9d0e1f2a3b4c5d6e",
  "expires": "2026-09-08T23:45:00Z"
}
```

> [!WARNING]
> Refresh tokens are **single-use**. Upon issuing a replacement token pair, the server invalidates the prior refresh token. Replaying an old refresh token results in HTTP `401 Unauthorized`.

---

## 2. Session Revocation & Logout Specification

### Method Signature

```kotlin
package pro.aduki.hermes.sdk

suspend fun HermesClient.logout(): Boolean
```

- **Parameters**: None.
- **Return Type**: `Boolean`. Returns `true` if the server acknowledged session invalidation (`200 OK` or `204 No Content`); `false` if network unreachable.
- **Security Action**: Regardless of network reachability, `session.clear()` is guaranteed to execute, zeroing in-memory buffers and erasing the encrypted KeyStore bundle.

### Network Wire Protocol

```http
POST /v1/auth/logout HTTP/1.1
Host: hermers.aduki.pro
Authorization: Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json; charset=utf-8

{}
```

---

## 3. Reactive Session State Repository

The SDK exposes `Session` for observing auth state throughout the Android application:

```kotlin
package pro.aduki.hermes.state.repository

class Session {
    val tokens: StateFlow<Tokens?>
    val identity: StateFlow<Identity?>

    fun token(): String?
    fun refresh(): String?
    fun update(tokens: Tokens)
    fun update(identity: Identity)
    fun clear()
}
```

### Observation Example in Jetpack Compose

```kotlin
@Composable
fun MainScreen(client: HermesClient) {
    val identity by client.session.identity.collectAsStateWithLifecycle()
    val tokens by client.session.tokens.collectAsStateWithLifecycle()

    if (tokens == null) {
        // User is logged out — navigate to AuthNavGraph
        LaunchedEffect(Unit) {
            navController.navigate("login") { popUpTo(0) }
        }
        return
    }

    Text("Logged in as ${identity?.user} (Tier: ${identity?.tier})")
}
```

