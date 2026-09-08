package pro.aduki.hermes.sdk

import pro.aduki.hermes.core.config.Endpoints
import pro.aduki.hermes.core.config.Options
import pro.aduki.hermes.state.repository.Identity
import pro.aduki.hermes.state.repository.SessionRepository

/**
 * HermesClient is the primary entrypoint for the Android Kotlin SDK.
 */
class HermesClient private constructor(
    val apiKey: String,
    val options: Options,
    val session: SessionRepository
) {
    val mail = Mail(this)
    val contacts = Contacts(this)
    val sync = Sync(this)

    /**
     * Retrieves currently resolved and cached identity.
     */
    fun me(): Identity? = session.identity.value

    class Builder {
        private var apiKey: String = ""
        private var endpoint: String = Endpoints.REST
        private var grpcHost: String = Endpoints.GRPC_HOST
        private var grpcPort: Int = Endpoints.GRPC_PORT
        private var secure: Boolean = true
        private var timeoutSeconds: Long = 15

        fun key(key: String) = apply { this.apiKey = key }
        fun endpoint(endpoint: String) = apply { this.endpoint = endpoint }
        fun grpcEndpoint(host: String, port: Int = 443) = apply {
            this.grpcHost = host
            this.grpcPort = port
        }
        fun secureStore(enabled: Boolean) = apply { this.secure = enabled }
        fun timeout(seconds: Long) = apply { this.timeoutSeconds = seconds }

        fun build(): HermesClient {
            require(apiKey.isNotBlank()) { "API key must not be blank" }
            val options = Options(
                endpoint = endpoint,
                grpcHost = grpcHost,
                grpcPort = grpcPort,
                timeoutSeconds = timeoutSeconds,
                secure = secure
            )
            val session = SessionRepository()
            return HermesClient(apiKey, options, session)
        }
    }

    companion object {
        fun builder() = Builder()
    }
}

