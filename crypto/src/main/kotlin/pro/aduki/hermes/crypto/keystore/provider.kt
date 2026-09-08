package pro.aduki.hermes.crypto.keystore

import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Provider abstracts hardware-backed KeyStore access (StrongBox / TEE) with JVM fallback.
 */
class Provider(private val type: String = "AndroidKeyStore") {

    companion object {
        const val MASTER = "hermes_master"
    }

    /**
     * Retrieves an existing key or creates a new one.
     */
    fun get(alias: String = MASTER): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(type).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.getKey(alias, null) as SecretKey
            } else {
                create(alias)
            }
        } catch (_: Exception) {
            // JVM fallback for unit tests and local execution
            createSoftware(alias)
        }
    }

    /**
     * Generates a new AES-256 key in the KeyStore.
     */
    fun create(alias: String): SecretKey {
        return try {
            val keyGen = KeyGenerator.getInstance("AES", type)
            keyGen.init(256)
            keyGen.generateKey()
        } catch (_: Exception) {
            createSoftware(alias)
        }
    }

    /**
     * Checks if a key alias exists.
     */
    fun has(alias: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(type).apply { load(null) }
            keyStore.containsAlias(alias)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Removes a key alias from the KeyStore.
     */
    fun remove(alias: String) {
        try {
            val keyStore = KeyStore.getInstance(type).apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        } catch (_: Exception) {
            // Ignored on software fallback
        }
    }

    private fun createSoftware(alias: String): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}
