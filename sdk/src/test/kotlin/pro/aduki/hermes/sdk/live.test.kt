package pro.aduki.hermes.sdk

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import pro.aduki.hermes.core.config.Endpoints

/**
 * Tier 5: Live Hermes Server Integration Tests (SERVER REQUIRED - PUT LAST).
 * Executes only when hermes.live=true or HERMES_LIVE=true is configured.
 */
class LiveTest {

    private fun isLiveEnabled(): Boolean {
        val prop = System.getProperty("hermes.live")
        val env = System.getenv("HERMES_LIVE")
        return prop == "true" || env == "true"
    }

    private fun liveApiKey(): String {
        return System.getProperty("hermes.key")
            ?: System.getenv("HERMES_KEY")
            ?: "hm_live_test_credential_hex"
    }

    private fun liveEndpoint(): String {
        return System.getProperty("hermes.endpoint")
            ?: System.getenv("HERMES_ENDPOINT")
            ?: Endpoints.REST
    }

    private fun liveGrpcHost(): String {
        return System.getProperty("hermes.grpc.host")
            ?: System.getenv("HERMES_GRPC_HOST")
            ?: Endpoints.GRPC_HOST
    }

    @Before
    fun requireServer() {
        assumeTrue("Skipping Tier 5 live server tests: hermes.live is not enabled", isLiveEnabled())
    }

    /**
     * T5-LIVE-01: Live Whoami Resolution
     */
    @Test
    fun whoami() = runBlocking {
        val client = HermesClient.builder()
            .key(liveApiKey())
            .endpoint(liveEndpoint())
            .build()

        val identity = client.me()
        assertNotNull("Expected live server identity resolution", identity)
        assertTrue(identity!!.user.isNotBlank())
        assertTrue(identity.tenant.isNotBlank())
    }

    /**
     * T5-LIVE-02: Live gRPC Connection Handshake
     */
    @Test
    fun connect() = runBlocking {
        val client = HermesClient.builder()
            .key(liveApiKey())
            .endpoint(liveEndpoint())
            .grpc(liveGrpcHost(), Endpoints.GRPC_PORT)
            .build()

        assertNotNull(client)
        assertTrue(client.lifecycle.active())
    }

    /**
     * T5-LIVE-03: Live Mailbox Listing
     */
    @Test
    fun mailboxes() = runBlocking {
        val client = HermesClient.builder()
            .key(liveApiKey())
            .endpoint(liveEndpoint())
            .build()

        val boxes = client.mail.mailboxes()?.value ?: emptyList()
        assertNotNull(boxes)
    }

    /**
     * T5-LIVE-04: Live CONDSTORE Delta Sync
     */
    @Test
    fun sync() = runBlocking {
        val client = HermesClient.builder()
            .key(liveApiKey())
            .endpoint(liveEndpoint())
            .build()

        val result = client.sync.all(listOf("inbox"))
        assertTrue(result)
    }

    /**
     * T5-LIVE-05: Live Outbox Send & Delivery
     */
    @Test
    fun send() = runBlocking {
        val client = HermesClient.builder()
            .key(liveApiKey())
            .endpoint(liveEndpoint())
            .build()

        val sent = client.mail.send(
            to = listOf("test@aduki.pro"),
            subject = "Automated Live Tier 5 Test",
            body = "Hermes Android SDK Live Verification"
        )
        assertNotNull(sent)
        assertTrue(sent.hex.isNotBlank())
    }
}

