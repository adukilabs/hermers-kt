package pro.aduki.hermes.net

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pro.aduki.hermes.net.http.AuthInterceptor

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testAuthHeaderInjected() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"ok"}"""))

        val apiKey = "hm_live_abc123xyz789"
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(apiKey))
            .build()

        val request = Request.Builder()
            .url(server.url("/auth/whoami"))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(200, response.code)
        }

        val recorded = server.takeRequest()
        assertEquals("Key $apiKey", recorded.getHeader("Authorization"))
        assertEquals("application/json", recorded.getHeader("Accept"))
        assertEquals("Hermes-Android/1.0.0", recorded.getHeader("User-Agent"))
    }
}

