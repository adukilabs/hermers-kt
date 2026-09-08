package pro.aduki.hermes.core.memory

import java.util.Arrays

/**
 * Deterministic memory zeroization for sensitive byte and character arrays.
 */
inline fun <R> withWipedBytes(size: Int, block: (ByteArray) -> R): R {
    val buffer = ByteArray(size)
    try {
        return block(buffer)
    } finally {
        Arrays.fill(buffer, 0.toByte())
    }
}

inline fun <R> withWipedChars(chars: CharArray, block: (CharArray) -> R): R {
    try {
        return block(chars)
    } finally {
        Arrays.fill(chars, '\u0000')
    }
}

fun ByteArray.wipe() {
    Arrays.fill(this, 0.toByte())
}

fun CharArray.wipe() {
    Arrays.fill(this, '\u0000')
}

