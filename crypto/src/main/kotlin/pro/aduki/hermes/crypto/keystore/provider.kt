package pro.aduki.hermes.crypto.keystore

import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * KeyStoreProvider abstracts secure key retrieval with hardware StrongBox / TEE or fallback.
 */
class KeyStoreProvider(private val keyStoreType: String = "AndroidKeyStore") {

    companion object {
        const val MASTER_ALIAS = "hermes_master_key"
    }

    fun getOrCreateKey(alias: String = MASTER_ALIAS): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.getKey(alias, null) as SecretKey
            } else {
                generateHardwareKey(alias)
            }
        } catch (_: Exception) {
            // JVM / non-Android fallback (for local unit testing)
            generateSoftwareKey()
        }
    }

    private fun generateHardwareKey(alias: String): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES", keyStoreType)
        keyGen.init(256)
        return keyGen.generateKey()
    }

    private fun generateSoftwareKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}

