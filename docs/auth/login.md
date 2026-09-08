# Interactive Login

Interactive authentication enables human users to log into Hermes using their email address and password.

---

## 1. Initiating Login

Use `HermesClient.login` inside your login ViewModel or CoroutineScope:

```kotlin
try {
    val client = HermesClient.login(
        email = "alice@aduki.pro",
        password = "CorrectHorseBatteryStaple123!",
        totp = null // Omit if user does not have 2FA enabled
    )

    // Login successful — client is fully configured and identity is cached
    val me = client.me()
    Log.d("Hermes", "Logged in as user ${me?.user} in tenant ${me?.tenant}")

} catch (e: HermesException.Unauthorized) {
    // Invalid credentials or missing TOTP
    Log.e("Hermes", "Login failed: ${e.message}")
} catch (e: HermesException.Network) {
    // Network connectivity failure
    Log.e("Hermes", "Network error: ${e.message}")
}
```

---

## 2. What Happens During Login

1. The SDK issues a `POST /v1/auth/login` request to the Hermes REST endpoint with `{ email, password, totp }`.
2. Upon receiving `200 OK`, the SDK extracts:
   - `token`: Short-lived access JWT (1-hour TTL).
   - `refresh`: Long-lived refresh token (30-day TTL).
   - `expires`: ISO-8601 timestamp.
3. The SDK stores both tokens securely in memory and in the hardware-backed `AndroidKeyStore`.
4. The SDK immediately invokes `GET /v1/auth/whoami` to populate the active user profile, tenant hex, and authorized permission scopes.
5. The initialized `HermesClient` instance is returned, ready for reactive UI observation.
