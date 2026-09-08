# TLS & Certificate Pinning

To safeguard against rogue Certificate Authorities (CAs) and adversary-in-the-middle (AITM) attacks, the Hermes Android SDK enforces strict transport layer security.

---

## 1. SPKI Certificate Pinning

The SDK utilizes SHA-256 Subject Public Key Info (SPKI) hashes pinned to the official Hermes production and staging infrastructure:

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("*.aduki.pro", "sha256/k2oTQLGenANUdY3TR1Wd5rvUgUhysw+TGgcUJWZChd4=")
    .add("*.aduki.pro", "sha256/FEzVOUp4dF3gI0ZVPRJhFbS1c49mmG820f18LMMAmrA=") // Backup root
    .build()
```

---

## 2. TLS 1.3 Restricted ConnectionSpec

Cleartext HTTP traffic is strictly prohibited. The SDK enforces TLS 1.3 and TLS 1.2 with forward-secret cipher suites:

- `TLS_AES_128_GCM_SHA256`
- `TLS_AES_256_GCM_SHA384`
- `TLS_CHACHA20_POLY1305_SHA256`
- `TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256`
- `TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256`

Insecure cipher suites (CBC mode, 3DES, RC4) and outdated protocols (TLS 1.0, 1.1) are rejected at the socket layer.
