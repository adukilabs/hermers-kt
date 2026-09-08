# Two-Factor Authentication (TOTP)

Hermes supports RFC 6238 Time-based One-Time Passwords (TOTP) for two-factor authentication (2FA).

---

## 1. Logging In with TOTP

When a user has TOTP enabled on their account, `POST /v1/auth/login` requires the current 6-digit code:

```kotlin
val client = HermesClient.login(
    email = "alice@aduki.pro",
    password = "CorrectHorseBatteryStaple123!",
    totp = "123456" // Exactly 6 numeric digits
)
```

If the account requires TOTP and none is provided, or if the provided code is incorrect/expired, the call throws `HermesException.Unauthorized`.

---

## 2. Enabling or Updating TOTP Confirmation

To confirm or update a TOTP configuration on an already-authenticated session, invoke `client.totp(code)`:

```kotlin
viewModelScope.launch {
    try {
        val success = client.totp("654321")
        if (success) {
            Log.d("Hermes", "TOTP two-factor authentication enabled")
        }
    } catch (e: Exception) {
        Log.e("Hermes", "Failed to confirm TOTP: ${e.message}")
    }
}
```

This issues a protected `PATCH /v1/user/totp` request with `Authorization: Bearer <jwt>`, persisting the TOTP status on the Hermes user account.
