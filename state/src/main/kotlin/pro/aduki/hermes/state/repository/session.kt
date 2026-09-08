package pro.aduki.hermes.state.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cached session and identity state repository.
 */
data class Identity(
    val user: String = "",
    val tenant: String = "",
    val owner: Boolean = false,
    val scopes: List<String> = emptyList(),
    val tier: String = ""
)

class SessionRepository {
    private val _identity = MutableStateFlow<Identity?>(null)
    val identity: StateFlow<Identity?> = _identity.asStateFlow()

    fun updateIdentity(newIdentity: Identity) {
        _identity.value = newIdentity
    }

    fun clear() {
        _identity.value = null
    }
}

