package pro.aduki.hermes.crypto.cipher

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import pro.aduki.hermes.crypto.keystore.KeyStoreProvider
import java.security.GeneralSecurityException

class EnvelopeTest {

    private val provider = KeyStoreProvider()

    @Test
    fun testRoundtrip() {
        val master = provider.getOrCreateKey("test_key")
        val plaintext = "Sensitive token: hm_live_1234567890".toByteArray(Charsets.UTF_8)

        val encrypted = EnvelopeCipher.encrypt(plaintext, master)
        val decrypted = EnvelopeCipher.decrypt(encrypted, master)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testUniqueIV() {
        val master = provider.getOrCreateKey("test_key_iv")
        val plaintext = "Payload".toByteArray(Charsets.UTF_8)

        val enc1 = EnvelopeCipher.encrypt(plaintext, master)
        val enc2 = EnvelopeCipher.encrypt(plaintext, master)

        val iv1 = enc1.copyOfRange(0, 12)
        val iv2 = enc2.copyOfRange(0, 12)
        assertFalse(iv1.contentEquals(iv2))
    }

    @Test
    fun testTamperDetection() {
        val master = provider.getOrCreateKey("test_key_tamper")
        val plaintext = "Payload".toByteArray(Charsets.UTF_8)
        val encrypted = EnvelopeCipher.encrypt(plaintext, master)

        // Corrupt one byte
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1] + 1).toByte()

        assertThrows(GeneralSecurityException::class.java) {
            EnvelopeCipher.decrypt(encrypted, master)
        }
    }
}
