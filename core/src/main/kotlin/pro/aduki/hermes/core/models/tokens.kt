package pro.aduki.hermes.core.models

/**
 * Tokens represents access and refresh session tokens.
 */
data class Tokens(
    val token: String = "",
    val refresh: String = "",
    val expires: String = ""
)

