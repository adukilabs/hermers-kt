# Memory Sanitization

Standard Java/Kotlin garbage collection does not guarantee when sensitive memory buffers (such as plaintext passwords or private keys) are overwritten. The Hermes SDK provides zeroization utilities to securely wipe memory buffers immediately after use.

---

## 1. Automated Scrubber (`Guard`)

The `Guard` class implements `AutoCloseable`, automatically zeroing byte or char buffers upon exiting a block:

```kotlin
val passwordChars = charArrayOf('S', 'e', 'c', 'u', 'r', 'e', '!')

Guard(passwordChars).use { guard ->
    // Use passwordChars safely inside this block
    authenticate(passwordChars)
}

// Outside the block, passwordChars contains only '\u0000'
```

---

## 2. Functional Scrubber Utilities

In `core/memory/wipe.kt`:

```kotlin
// Secure byte array execution
val result = withWipedBytes(byteArrayOf(1, 2, 3, 4)) { bytes ->
    processKey(bytes)
}

// Secure char array execution
val token = withWipedChars(rawPassword.toCharArray()) { chars ->
    deriveKey(chars)
}
```

Both utilities guarantee `Arrays.fill(0)` is invoked in a `finally` block, ensuring memory is cleansed even if an uncaught exception is thrown.
