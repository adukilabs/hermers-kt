package pro.aduki.hermes.crypto.cipher

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM Envelope cipher for encrypting database keys and sensitive attachments.
 */
object EnvelopeCipher {
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    fun encrypt(plainBytes: ByteArray, masterKey: SecretKey): ByteArray {
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val cipherText = cipher.doFinal(plainBytes)

        // Structure: [12B IV] + [Ciphertext + Tag]
        val result = ByteArray(IV_LENGTH + cipherText.size)
        System.arraycopy(iv, 0, result, 0, IV_LENGTH)
        System.arraycopy(cipherText, 0, result, IV_LENGTH, cipherText.size)
        return result
    }

    fun decrypt(encryptedBytes: ByteArray, masterKey: SecretKey): ByteArray {
        require(encryptedBytes.size > IV_LENGTH) { "Ciphertext too short to contain IV" }
        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(encryptedBytes, 0, iv, 0, IV_LENGTH)

        val cipherTextLength = encryptedBytes.size - IV_LENGTH
        val cipherText = ByteArray(cipherTextLength)
        System.arraycopy(encryptedBytes, IV_LENGTH, cipherText, 0, cipherTextLength)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(cipherText)
    }
}

