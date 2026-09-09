package pro.aduki.hermes.net.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pro.aduki.hermes.core.errors.HermesException
import java.io.IOException
import pro.aduki.hermes.core.models.Tokens

/**
 * Type alias for Tokens.
 */
typealias Tokens = pro.aduki.hermes.core.models.Tokens


/**
 * Login handles interactive credential authentication, TOTP 2FA, session refresh, and logout.
 */
object Login {

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Submits email, password, and optional 6-digit TOTP confirmation to POST /auth/login.
     */
    fun submit(
        client: OkHttpClient,
        endpoint: String,
        email: String,
        password: String,
        totp: String? = null
    ): Tokens {
        val root = JSONObject().apply {
            put("email", email)
            put("password", password)
            if (!totp.isNullOrBlank()) {
                put("totp", totp.trim())
            }
        }

        val url = "${endpoint.trimEnd('/')}/auth/login"
        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 401) {
                throw HermesException.Unauthorized("Invalid credentials or invalid TOTP code")
            }
            if (!response.isSuccessful) {
                throw HermesException.Network("Login failed with HTTP ${response.code}")
            }

            val body = response.body?.string() ?: throw HermesException.Network("Empty response from login")
            val json = JSONObject(body)
            return Tokens(
                token = json.optString("token", ""),
                refresh = json.optString("refresh", ""),
                expires = json.optString("expires", "")
            )
        }
    }

    /**
     * Exchanges a refresh token for a new token pair via POST /auth/refresh.
     */
    fun refresh(
        client: OkHttpClient,
        endpoint: String,
        refreshToken: String
    ): Tokens {
        val root = JSONObject().apply {
            put("token", refreshToken)
        }

        val url = "${endpoint.trimEnd('/')}/auth/refresh"
        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 401) {
                throw HermesException.Unauthorized("Refresh token expired or revoked")
            }
            if (!response.isSuccessful) {
                throw HermesException.Network("Token refresh failed with HTTP ${response.code}")
            }

            val body = response.body?.string() ?: throw HermesException.Network("Empty response from refresh")
            val json = JSONObject(body)
            return Tokens(
                token = json.optString("token", ""),
                refresh = json.optString("refresh", ""),
                expires = json.optString("expires", "")
            )
        }
    }

    /**
     * Revokes active session on server via POST /auth/logout.
     */
    fun logout(
        client: OkHttpClient,
        endpoint: String,
        token: String
    ): Boolean {
        val url = "${endpoint.trimEnd('/')}/auth/logout"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", if (token.startsWith("hm_")) "Key $token" else "Bearer $token")
            .post("{}".toRequestBody(JSON))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: IOException) {
            false
        }
    }

    /**
     * Configures or confirms 6-digit TOTP secret via PATCH /user/totp.
     */
    fun totp(
        client: OkHttpClient,
        endpoint: String,
        token: String,
        code: String
    ): Boolean {
        require(code.length == 6 && code.all { it.isDigit() }) { "TOTP code must be exactly 6 digits" }

        val url = "${endpoint.trimEnd('/')}/user/totp"
        val payload = JSONObject.quote(code) // Valid JSON string representation
        val request = Request.Builder()
            .url(url)
            .header("Authorization", if (token.startsWith("hm_")) "Key $token" else "Bearer $token")
            .patch(payload.toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 401) {
                throw HermesException.Unauthorized("Session expired or unauthorized")
            }
            return response.isSuccessful
        }
    }
}

