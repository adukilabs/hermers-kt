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
 * Tokens models interactive access and refresh session tokens.
 */
data class Tokens(
    val token: String = "",
    val refresh: String = "",
    val expires: String = ""
)

/**
 * Session manages active identity and token StateFlows.
 */
class Session {
    private val _identity = MutableStateFlow<Identity?>(null)
    val identity: StateFlow<Identity?> = _identity.asStateFlow()

    private val _tokens = MutableStateFlow<Tokens?>(null)
    val tokens: StateFlow<Tokens?> = _tokens.asStateFlow()

    fun update(identity: Identity) {
        _identity.value = identity
    }

    fun update(tokens: Tokens) {
        _tokens.value = tokens
    }

    fun updateIdentity(newIdentity: Identity) = update(newIdentity)

    fun token(): String? = _tokens.value?.token

    fun refresh(): String? = _tokens.value?.refresh

    fun clear() {
        _identity.value = null
        _tokens.value = null
    }
}

typealias SessionRepository = Session
