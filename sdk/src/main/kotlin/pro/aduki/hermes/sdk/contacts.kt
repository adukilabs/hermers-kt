package pro.aduki.hermes.sdk

import pro.aduki.hermes.store.entities.Contact

/**
 * Contacts service providing high-level operations for address book.
 */
class Contacts internal constructor(private val client: HermesClient) {

    suspend fun list(): List<Contact> {
        return emptyList()
    }
}

