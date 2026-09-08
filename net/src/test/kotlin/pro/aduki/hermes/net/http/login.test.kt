package pro.aduki.hermes.net.http

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pro.aduki.hermes.core.errors.HermesException

class LoginTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun testSubmitPasswordOnly() {
        val jsonResponse = """
            {
                "token": "jwt_access_123",
                "refresh": "rt_refresh_456",
                "expires": "2026-09-08T22:00:00Z"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val endpoint = server.url("/v1").toString()
        val tokens = Login.submit(client, endpoint, "user@aduki.pro", "password123")

        assertEquals("jwt_access_123", tokens.token)
        assertEquals("rt_refresh_456", tokens.refresh)
        assertEquals("2026-09-08T22:00:00Z", tokens.expires)

        val recorded = server.takeRequest()
        assertEquals("/v1/auth/login", recorded.path)
        val body = JSONObject(recorded.body.readUtf8())
        assertEquals("user@aduki.pro", body.getString("email"))
        assertEquals("password123", body.getString("password"))
        assertTrue(!body.has("totp"))
    }

    @Test
    fun testSubmitWithTotp() {
        val jsonResponse = """
            {
                "token": "jwt_totp_verified",
                "refresh": "rt_totp_refresh",
                "expires": "2026-09-08T23:00:00Z"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val endpoint = server.url("/v1").toString()
        val tokens = Login.submit(client, endpoint, "user@aduki.pro", "password123", "654321")

        assertEquals("jwt_totp_verified", tokens.token)
        val recorded = server.takeRequest()
        val body = JSONObject(recorded.body.readUtf8())
        assertEquals("654321", body.getString("totp"))
    }

    @Test
    fun testSubmitUnauthorized() {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error": "unauthorized"}"""))

        val endpoint = server.url("/v1").toString()
        assertThrows(HermesException.Unauthorized::class.java) {
            Login.submit(client, endpoint, "bad@aduki.pro", "wrong_pass")
        }
    }

    @Test
    fun testRefreshToken() {
        val jsonResponse = """
            {
                "token": "jwt_rotated_789",
                "refresh": "rt_new_000",
                "expires": "2026-09-09T00:00:00Z"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val endpoint = server.url("/v1").toString()
        val tokens = Login.refresh(client, endpoint, "rt_old_111")

        assertEquals("jwt_rotated_789", tokens.token)
        assertEquals("rt_new_000", tokens.refresh)

        val recorded = server.takeRequest()
        assertEquals("/v1/auth/refresh", recorded.path)
        val body = JSONObject(recorded.body.readUtf8())
        assertEquals("rt_old_111", body.getString("token"))
    }

    @Test
    fun testLogout() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok": true}"""))

        val endpoint = server.url("/v1").toString()
        val ok = Login.logout(client, endpoint, "jwt_to_revoke")

        assertTrue(ok)
        val recorded = server.takeRequest()
        assertEquals("/v1/auth/logout", recorded.path)
        assertEquals("Bearer jwt_to_revoke", recorded.getHeader("Authorization"))
    }

    @Test
    fun testTotpConfirmation() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"totp": true}"""))

        val endpoint = server.url("/v1").toString()
        val ok = Login.totp(client, endpoint, "jwt_user_token", "123456")

        assertTrue(ok)
        val recorded = server.takeRequest()
        assertEquals("/v1/user/totp", recorded.path)
        assertEquals("Bearer jwt_user_token", recorded.getHeader("Authorization"))
        assertEquals("\"123456\"", recorded.body.readUtf8())
    }
}

