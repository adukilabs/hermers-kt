# Authentication Overview

The Hermes Android SDK supports two distinct authentication schemes designed for different security contexts:

1. **Interactive User Sessions (JWT + Refresh + TOTP)**: Designed for user-facing applications. The user logs in interactively with their email, password, and optional 6-digit TOTP code. The SDK obtains a short-lived access JWT and refresh token, automatically handling silent background refresh and session revocation.
2. **Static API Keys**: Designed for headless daemons, automated services, or embedded hardware. Uses immutable cryptographic keys prefixed with `hm_live_...`.

---

## Transport Headers

| Auth Scheme | HTTP Header | gRPC Metadata Header | Notes |
| :--- | :--- | :--- | :--- |
| **Interactive JWT** | `Authorization: Bearer <jwt>` | `authorization: Bearer <jwt>` | 1-hour access TTL, rotated automatically via refresh token |
| **Static API Key** | `Authorization: Key hm_...` | `authorization: Key hm_...` | Immutable token, verified on every call |

---

## Token Security

Session tokens are protected using **hardware-backed envelope encryption**:

- AES-256-GCM master key isolated in the `AndroidKeyStore`.
- On-device zero-plaintext storage.
- Auto-zeroed in-memory secrets on logout or process exit.
