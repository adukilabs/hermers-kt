package pro.aduki.hermes.state.repository

import io.objectbox.BoxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pro.aduki.hermes.store.entities.Contact as ContactEntity

/**
 * ContactSource abstracts reactive entity streams for contacts.
 */
interface ContactSource {
    fun contacts(): Flow<List<ContactEntity>>
    fun get(hex: String): ContactEntity?
}

/**
 * Contact repository exposing reactive address book StateFlow query streams.
 */
class Contact(
    private val source: ContactSource,
    private val scope: CoroutineScope
) {

    constructor(store: BoxStore, scope: CoroutineScope) : this(
        source = object : ContactSource {
            private val box = store.boxFor(ContactEntity::class.java)
            override fun contacts(): Flow<List<ContactEntity>> = MutableStateFlow(box.all)
            override fun get(hex: String): ContactEntity? = box.all.firstOrNull { it.hex == hex }
        },
        scope = scope
    )

    /**
     * Hot StateFlow of all contacts sorted by name.
     */
    fun observe(): StateFlow<List<ContactEntity>> {
        return source.contacts()
            .map { list -> list.sortedBy { it.name } }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Hot StateFlow of contacts matching a search prefix or substring.
     */
    fun search(query: String): StateFlow<List<ContactEntity>> {
        val q = query.trim().lowercase()
        return source.contacts()
            .map { list ->
                if (q.isEmpty()) {
                    list.sortedBy { it.name }
                } else {
                    list.filter {
                        it.name.lowercase().contains(q) ||
                                it.email.lowercase().contains(q) ||
                                it.phone.lowercase().contains(q)
                    }.sortedBy { it.name }
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Looks up a contact synchronously by hex.
     */
    fun get(hex: String): ContactEntity? = source.get(hex)
}

typealias ContactRepository = Contact

