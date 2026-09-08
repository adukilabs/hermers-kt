# Token Lifecycle & Session Management

Interactive sessions operate on short-lived JWT access tokens and long-lived refresh tokens.

---

## 1. Token Refresh

Access JWTs expire in **1 hour**. The SDK manages token rotation automatically when making requests:

If you wish to trigger a manual token refresh:

```kotlin
val ok = client.refresh()
if (ok) {
    Log.d("Hermes", "Session tokens rotated successfully")
}
```

Behind the scenes:

1. SDK calls `POST /v1/auth/refresh` with `{ "token": "<refresh_token>" }`.
2. Hermes returns a fresh access JWT and a newly rotated refresh token.
3. The previous refresh token is immediately invalidated on the server.
4. Active `Session` StateFlow emits the updated tokens.

---

## 2. Session Revocation & Logout

To terminate a session and revoke credentials:

```kotlin
viewModelScope.launch {
    client.logout()
    // Session state is wiped from memory and Android KeyStore
    // User can be safely navigated to the login screen
}
```

The `logout()` operation:

1. Calls `POST /v1/auth/logout` with `Authorization: Bearer <jwt>` to revoke the session in Redis/database on the server.
2. Zeroes in-memory token state in `Session`.
3. Erases the encrypted token bundle from the local KeyStore vault.
