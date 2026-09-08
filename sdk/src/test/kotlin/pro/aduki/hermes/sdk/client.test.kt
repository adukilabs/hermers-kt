package pro.aduki.hermes.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ClientTest {

    @Test
    fun testBuilder() {
        val client = HermesClient.builder()
            .key("hm_live_validkey123")
            .endpoint("https://hermers.aduki.pro/v1")
            .timeout(30)
            .secureStore(true)
            .build()

        assertNotNull(client)
        assertEquals("hm_live_validkey123", client.apiKey)
        assertEquals("https://hermers.aduki.pro/v1", client.options.endpoint)
        assertEquals(30L, client.options.timeoutSeconds)
        assertNotNull(client.mail)
        assertNotNull(client.contacts)
        assertNotNull(client.sync)
    }

    @Test
    fun testEmptyKey() {
        assertThrows(IllegalArgumentException::class.java) {
            HermesClient.builder()
                .key("")
                .build()
        }
    }
}
