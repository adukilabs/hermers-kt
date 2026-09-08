# Android KeyStore Integration

The `KeyStoreProvider` class encapsulates key lifecycle management inside the `AndroidKeyStore`.

---

## 1. Hardware Backing: StrongBox & TEE

When generating keys on Android, the SDK requests a dedicated Secure Element (StrongBox):

- **StrongBox Keymaster (Dedicated SE)**: Independent CPU and memory chip with tamper-resistant packaging.
- **Trusted Execution Environment (TEE)**: Hardware-isolated processor environment running concurrently with the main Android OS.
- **JVM Fallback**: Used exclusively during local unit testing when Android native KeyStore is unavailable.

---

## 2. Implementation

The master key is generated with:

- **Algorithm**: `AES` (Advanced Encryption Standard).
- **Key Size**: `256 bits`.
- **Block Mode**: `GCM` (Galois/Counter Mode).
- **Padding**: `NoPadding`.
- **Purpose**: `PURPOSE_ENCRYPT or PURPOSE_DECRYPT`.

```kotlin
val provider = Provider()
val secretKey = provider.get(Provider.MASTER)
```

The alias `hermes_master` is persistent; subsequent invocations retrieve the existing key without regeneration.
