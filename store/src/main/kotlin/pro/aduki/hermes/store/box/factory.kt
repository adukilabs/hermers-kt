package pro.aduki.hermes.store.box

import io.objectbox.BoxStore
import io.objectbox.BoxStoreBuilder
import java.io.File

/**
 * Factory for configuring BoxStore with native encryption.
 */
object Factory {

    /**
     * Builds BoxStore configuration targeting specified directory and optional encryption key.
     */
    fun create(dir: File, key: ByteArray? = null): BoxStoreBuilder {
        val builder = BoxStoreBuilder(dir)
        if (key != null) {
            builder.initialBytes(key)
        }
        return builder
    }
}
