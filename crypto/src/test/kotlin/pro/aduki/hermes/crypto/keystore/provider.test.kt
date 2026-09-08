package pro.aduki.hermes.crypto.keystore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProviderTest {

    private val provider = Provider()

    @Test
    fun testGetGeneratesKey() {
        val key = provider.get("alias_alpha")
        assertNotNull(key)
        assertEquals("AES", key.algorithm)
    }

    @Test
    fun testCreateNewKey() {
        val key = provider.create("alias_beta")
        assertNotNull(key)
        assertEquals(32, key.encoded.size) // 256 bits = 32 bytes
    }
}

