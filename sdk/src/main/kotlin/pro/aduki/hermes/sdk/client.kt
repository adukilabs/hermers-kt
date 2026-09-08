package pro.aduki.hermes.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import pro.aduki.hermes.core.config.Endpoints
import pro.aduki.hermes.core.config.Options
import pro.aduki.hermes.net.http.Client as HttpClient
import pro.aduki.hermes.net.http.Whoami
import pro.aduki.hermes.state.repository.Identity
import pro.aduki.hermes.state.repository.Session
import pro.aduki.hermes.sync.engine.Contact as ContactEngine
import pro.aduki.hermes.sync.engine.Mailbox as MailboxEngine
import pro.aduki.hermes.sync.outbox.Manager
import pro.aduki.hermes.sync.outbox.Worker
import pro.aduki.hermes.state.repository.Contact as ContactRepo
import pro.aduki.hermes.state.repository.Mail as MailRepo

/**
 * HermesClient is the primary entrypoint for the Android Kotlin SDK.
 */
class HermesClient internal constructor(
    val apiKey: String,
    val options: Options,
    val session: Session = Session(),
    val lifecycle: Lifecycle = Lifecycle(),
    private val httpClient: OkHttpClient? = null,
    manager: Manager? = null,
    worker: Worker? = null,
    mailRepo: MailRepo? = null,
    contactRepo: ContactRepo? = null,
    mailboxEngine: MailboxEngine? = null,
    contactEngine: ContactEngine? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    val mail = Mail(this, manager, mailRepo, worker)
    val contacts = Contacts(this, contactRepo, contactEngine)
    val sync = Sync(this, mailboxEngine, contactEngine, worker, manager)

    init {
        lifecycle.listen { active ->
            if (active) {
                scope.launch {
                    sync.flush()
                }
            }
        }
    }

    /**
     * Resolves authenticated user and tenant identity, utilizing cache if already resolved.
     */
    suspend fun me(): Identity? {
        val cached = session.identity.value
        if (cached != null) return cached

        val client = httpClient ?: HttpClient.create(apiKey, options.timeoutSeconds)
        return try {
            val resolved = Whoami.resolve(client, options.endpoint)
            session.update(resolved)
            resolved
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Suspends background operations when app enters background.
     */
    fun pause() {
        lifecycle.pause()
    }

    /**
     * Resumes background operations and flushes outbox when app enters foreground.
     */
    fun resume() {
        lifecycle.resume()
    }

    class Builder {
        private var apiKey: String = ""
        private var endpoint: String = Endpoints.REST
        private var grpcHost: String = Endpoints.GRPC_HOST
        private var grpcPort: Int = Endpoints.GRPC_PORT
        private var secure: Boolean = true
        private var timeoutSeconds: Long = 15
        private var httpClient: OkHttpClient? = null
        private var manager: Manager? = null
        private var worker: Worker? = null
        private var mailRepo: MailRepo? = null
        private var contactRepo: ContactRepo? = null
        private var mailboxEngine: MailboxEngine? = null
        private var contactEngine: ContactEngine? = null

        fun key(key: String) = apply { this.apiKey = key }
        fun endpoint(endpoint: String) = apply { this.endpoint = endpoint }
        fun grpc(host: String, port: Int = Endpoints.GRPC_PORT) = apply {
            this.grpcHost = host
            this.grpcPort = port
        }
        fun secure(enabled: Boolean) = apply { this.secure = enabled }
        fun timeout(seconds: Long) = apply { this.timeoutSeconds = seconds }
        fun http(client: OkHttpClient) = apply { this.httpClient = client }
        fun manager(manager: Manager) = apply { this.manager = manager }
        fun worker(worker: Worker) = apply { this.worker = worker }
        fun mail(repo: MailRepo) = apply { this.mailRepo = repo }
        fun contacts(repo: ContactRepo) = apply { this.contactRepo = repo }
        fun engines(mailbox: MailboxEngine, contact: ContactEngine) = apply {
            this.mailboxEngine = mailbox
            this.contactEngine = contact
        }

        fun build(): HermesClient {
            require(apiKey.isNotBlank()) { "API key must not be blank" }
            val options = Options(
                endpoint = endpoint,
                grpcHost = grpcHost,
                grpcPort = grpcPort,
                timeoutSeconds = timeoutSeconds,
                secure = secure
            )
            return HermesClient(
                apiKey = apiKey,
                options = options,
                httpClient = httpClient,
                manager = manager,
                worker = worker,
                mailRepo = mailRepo,
                contactRepo = contactRepo,
                mailboxEngine = mailboxEngine,
                contactEngine = contactEngine
            )
        }
    }

    companion object {
        fun builder() = Builder()
    }
}
