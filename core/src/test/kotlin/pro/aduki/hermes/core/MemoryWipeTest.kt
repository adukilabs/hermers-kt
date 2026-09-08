package pro.aduki.hermes.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pro.aduki.hermes.core.memory.wipe
import pro.aduki.hermes.core.memory.withWipedBytes
import pro.aduki.hermes.core.memory.withWipedChars

class MemoryWipeTest {

    @Test
    fun testByteArrayWiping() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        bytes.wipe()
        assertTrue(bytes.all { it == 0.toByte() })
    }

    @Test
    fun testCharArrayWiping() {
        val chars = charArrayOf('s', 'e', 'c', 'r', 'e', 't')
        chars.wipe()
        assertTrue(chars.all { it == '\u0000' })
    }

    @Test
    fun testWithWipedBytesScoping() {
        var externalRef: ByteArray? = null
        val result = withWipedBytes(32) { buf ->
            buf[0] = 42
            externalRef = buf
            "done"
        }
        assertEquals("done", result)
        assertTrue(externalRef!!.all { it == 0.toByte() })
    }

    @Test
    fun testWithWipedCharsScopingOnException() {
        val chars = charArrayOf('p', 'a', 's', 's')
        try {
            withWipedChars(chars) {
                throw IllegalStateException("Intentional failure")
            }
        } catch (_: Exception) {
            // Expected
        }
        assertTrue(chars.all { it == '\u0000' })
    }
}

