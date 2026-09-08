# Envelope Cipher

The `Envelope` cipher provides authenticated encryption with associated data (AEAD) using AES-256-GCM.

---

## 1. Cryptographic Specifications

- **Cipher Transformation**: `AES/GCM/NoPadding`
- **Key Length**: 256 bits (32 bytes)
- **Initialization Vector (IV)**: 96 bits (12 bytes), generated freshly using `SecureRandom` on every encryption.
- **Authentication Tag**: 128 bits (16 bytes), verifying ciphertext authenticity and preventing chosen-ciphertext attacks.

---

## 2. Serialization Format

Encrypted payloads are serialized with a 1-byte version header:

```text
┌─────────┬──────────────┬──────────────────────────────┬──────────────────┐
│ Version │ IV (12 bytes)│ Ciphertext (variable length) │ Tag (16 bytes)   │
│ 1 byte  │              │                              │ (appended in GCM)│
└─────────┴──────────────┴──────────────────────────────┴──────────────────┘
```

---

## 3. Usage Example

```kotlin
val cipher = Envelope(masterSecretKey)

// Encrypt database encryption key or sensitive payload
val encryptedBytes = cipher.encrypt(plaintextBytes)

// Decrypt with AEAD tag verification
val decryptedBytes = cipher.decrypt(encryptedBytes)
```

Modifying any bit of the ciphertext or tag causes `AEADBadTagException` during decryption, immediately halting execution.
