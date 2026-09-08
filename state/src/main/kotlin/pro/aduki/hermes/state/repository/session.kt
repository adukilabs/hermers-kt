package pro.aduki.hermes.state.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Identity models authenticated user and tenant state.
 */
data class Identity(
    val user: String = "",
    val tenant: String = "",
    val owner: Boolean = false,
    val scopes: List<String> = emptyList(),
    val tier: String = ""
)

/**
 * Session manages the active identity StateFlow.
 */
class Session {
    private val _identity = MutableStateFlow<Identity?>(null)
    val identity: StateFlow<Identity?> = _identity.asStateFlow()

    fun update(identity: Identity) {
        _identity.value = identity
    }

    fun updateIdentity(newIdentity: Identity) = update(newIdentity)

    fun clear() {
        _identity.value = null
    }
}

typealias SessionRepository = Session
