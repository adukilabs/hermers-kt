package pro.aduki.hermes.core.errors

/**
 * Sealed exception hierarchy for Hermes SDK.
 */
sealed class HermesException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Network(message: String, cause: Throwable? = null, val code: Int? = null) :
        HermesException(message, cause)

    class Auth(message: String, cause: Throwable? = null) :
        HermesException(message, cause)

    class Storage(message: String, cause: Throwable? = null) :
        HermesException(message, cause)

    class Sync(message: String, cause: Throwable? = null) :
        HermesException(message, cause)

    class Protocol(message: String, cause: Throwable? = null) :
        HermesException(message, cause)

    class CircuitOpen(message: String = "Circuit breaker is open") :
        HermesException(message)
}

