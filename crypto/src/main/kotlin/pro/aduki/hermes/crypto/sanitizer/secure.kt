package pro.aduki.hermes.crypto.sanitizer

import pro.aduki.hermes.core.memory.wipe
import java.io.Closeable

/**
 * SecureKey holds a mutable key byte array and automatically wipes it on close.
 */
class SecureKey(val bytes: ByteArray) : Closeable {
    override fun close() {
        bytes.wipe()
    }
}

inline fun <R> withSecureKey(bytes: ByteArray, block: (SecureKey) -> R): R {
    val key = SecureKey(bytes)
    return try {
        block(key)
    } finally {
        key.close()
    }
}

