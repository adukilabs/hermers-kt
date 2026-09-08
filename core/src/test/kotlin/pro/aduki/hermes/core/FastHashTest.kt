package pro.aduki.hermes.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import pro.aduki.hermes.core.memory.FastHash

class FastHashTest {

    @Test
    fun testEmptyDataHash() {
        val hash = FastHash.hash64(byteArrayOf())
        // xxHash64 with empty buffer produces deterministic seed-dependent hash
        assertNotEquals(0L, hash)
    }

    @Test
    fun testDeterministicConsistency() {
        val data = "Hermes Android SDK ultra-fast email client".toByteArray(Charsets.UTF_8)
        val hash1 = FastHash.hash64(data)
        val hash2 = FastHash.hash64(data)
        assertEquals(hash1, hash2)
    }

    @Test
    fun testDifferentPayloadsProduceDifferentHashes() {
        val data1 = "message_uid_1001".toByteArray(Charsets.UTF_8)
        val data2 = "message_uid_1002".toByteArray(Charsets.UTF_8)
        assertNotEquals(FastHash.hash64(data1), FastHash.hash64(data2))
    }
}

