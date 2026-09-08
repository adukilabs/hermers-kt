package pro.aduki.hermes.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import pro.aduki.hermes.crypto.cipher.EnvelopeCipher
import pro.aduki.hermes.crypto.keystore.KeyStoreProvider
import java.security.GeneralSecurityException

class EnvelopeCipherTest {

    private val keyProvider = KeyStoreProvider()

    @Test
    fun testEncryptionAndDecryptionRoundtrip() {
        val masterKey = keyProvider.getOrCreateKey("test_key")
        val plaintext = "Sensitive user authentication token: hm_live_1234567890".toByteArray(Charsets.UTF_8)

        val encrypted = EnvelopeCipher.encrypt(plaintext, masterKey)
        val decrypted = EnvelopeCipher.decrypt(encrypted, masterKey)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun testUniqueIVPerEncryption() {
        val masterKey = keyProvider.getOrCreateKey("test_key_iv")
        val plaintext = "Same payload twice".toByteArray(Charsets.UTF_8)

        val enc1 = EnvelopeCipher.encrypt(plaintext, masterKey)
        val enc2 = EnvelopeCipher.encrypt(plaintext, masterKey)

        // IVs are in first 12 bytes; they must be distinct
        val iv1 = enc1.copyOfRange(0, 12)
        val iv2 = enc2.copyOfRange(0, 12)
        assertFalse(iv1.contentEquals(iv2))
    }

    @Test
    fun testTamperedCiphertextThrowsAEADException() {
        val masterKey = keyProvider.getOrCreateKey("test_key_tamper")
        val plaintext = "Tamper check payload".toByteArray(Charsets.UTF_8)
        val encrypted = EnvelopeCipher.encrypt(plaintext, masterKey)

        // Corrupt one byte of ciphertext
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1] + 1).toByte()

        assertThrows(GeneralSecurityException::class.java) {
            EnvelopeCipher.decrypt(encrypted, masterKey)
        }
    }
}

