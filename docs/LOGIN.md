# Interactive Login & TOTP Authentication Specification

This document details the architecture, REST contracts, token lifecycle, and Android KeyStore storage for interactive user authentication (Email/Password), Two-Factor Authentication (TOTP), and automatic session rotation in the Hermes Android Kotlin SDK.

---

## 1. Overview & Rationale

While backend microservices and CLI tools interact with Hermes using static API keys (`Authorization: Key hm_live_...`), human users on Android authenticate interactively using:

1. **Primary Credentials**: User email address and password.
2. **Two-Factor Authentication (TOTP)**: 6-digit Time-based One-Time Password when 2FA is enabled.
3. **Session Tokens**: Short-lived JWT access token (1-hour TTL) and long-lived refresh token (30-day TTL).
4. **Hardware Storage**: Tokens encrypted via AES-256-GCM using keys isolated inside the `AndroidKeyStore` (StrongBox Keymaster or TEE).

```text
┌─────────────────────────────────────────────────────────────┐
│                       Android User                          │
│         (Inputs email, password, and optional TOTP)         │
└──────────────────────────────┬──────────────────────────────┘
                               │ POST /v1/auth/login
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Hermes REST Gateway                      │
│            (Verifies password & 6-digit TOTP)               │
└──────────────────────────────┬──────────────────────────────┘
                               │ 200 OK: TokenPair (JWT + Refresh)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│               Android KeyStore Security Layer               │
│   (AES-256-GCM Envelope encryption before flash storage)    │
└──────────────────────────────┬──────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
  REST Calls: Bearer <jwt>        gRPC Calls: Bearer <jwt>
  (Auto-refreshed on 401)         (Metadata CallCredentials)
```

---

## 2. Wire Protocols & Payloads

### 2.1. `POST /v1/auth/login` (Public)

Authenticates user credentials and issues a session token pair.

#### Request Body
```json
{
  "email": "user@aduki.pro",
  "password": "CorrectHorseBatteryStaple123!",
  "totp": "123456"
}
```

| Field | Type | Required | Notes |
| :--- | :--- | :---: | :--- |
| `email` | String | Yes | Valid email address |
| `password` | String | Yes | Plaintext password |
| `totp` | String | Conditional | Exactly 6 digits; required if TOTP is enabled on the account |

#### Response `200 OK`
```json
{
  "token": "eyJhbGciOiJSUzI1NiIs...",
  "refresh": "rt_01HZ9...",
  "expires": "2026-09-08T21:30:00Z"
}
```

#### Error Responses
- `401 Unauthorized`: Invalid credentials, wrong password, or missing/invalid 6-digit TOTP.

---

### 2.2. `POST /v1/auth/refresh` (Public)

Rotates an existing refresh token into a new JWT access token and fresh refresh token. The previous refresh token is immediately invalidated on the server.

#### Request Body
```json
{
  "token": "rt_01HZ9..."
}
```

#### Response `200 OK`
```json
{
  "token": "eyJhbGciOiJSUzI1NiIs...",
  "refresh": "rt_02JA1...",
  "expires": "2026-09-08T22:30:00Z"
}
```

---

### 2.3. `PATCH /v1/user/totp` (Protected - Bearer Auth)

Configures or confirms the user's 6-digit TOTP secret.

#### Request Body
```json
"123456"
```

#### Response `200 OK`
Returns the updated `User` representation with `totp: true`.

---

### 2.4. `POST /v1/auth/logout` (Protected - Bearer Auth)

Revokes the active session on the Hermes server and clears cached tokens.

#### Request Body
*None (Empty)*

#### Response `200 OK`
```json
{
  "ok": true
}
```

---

## 3. Token Security & Storage Architecture

1. **Zero Plaintext Flash Storage**:
   Tokens are never stored in unencrypted `SharedPreferences` or SQLite. They are encrypted using `EnvelopeCipher` (AES-256-GCM) with an authenticated KeyStore master key.
2. **Dual Auth Scheme in Transport**:
   The `AuthInterceptor` checks if the client is using an API key or an interactive JWT:
   - If API key: `Authorization: Key hm_live_...`
   - If interactive login: `Authorization: Bearer <jwt>`
3. **Transparent Token Refresh**:
   When OkHttp encounters an HTTP `401 Unauthorized` response, an `Authenticator` intercepts the failure, executes `POST /v1/auth/refresh` using the stored refresh token, updates the session tokens in memory and KeyStore, and replays the failed request with zero interruption to the user.

---

## 4. Kotlin SDK Facade API

### 4.1. Interactive Login
```kotlin
// Login with optional TOTP confirmation code
val client = HermesClient.login(
    email = "user@aduki.pro",
    password = "CorrectHorseBatteryStaple123!",
    totp = "123456"
)
```

### 4.2. Enabling TOTP Confirmation
```kotlin
// Configure or update TOTP on authenticated account
client.totp("654321")
```

### 4.3. Logout
```kotlin
// Revokes session on server and wipes encrypted tokens locally
client.logout()
```
