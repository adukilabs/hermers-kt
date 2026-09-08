package pro.aduki.hermes.net.http

import okhttp3.OkHttpClient
import okhttp3.Request
import pro.aduki.hermes.core.errors.HermesException
import pro.aduki.hermes.state.repository.Identity

/**
 * Whoami resolves and caches authenticated session identity via GET /auth/whoami.
 */
class Whoami(
    private val client: OkHttpClient,
    private val endpoint: String
) {

    /**
     * Resolves authenticated identity from the Hermes server.
     */
    fun resolve(): Identity {
        val url = if (endpoint.endsWith("/")) "${endpoint}auth/whoami" else "$endpoint/auth/whoami"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw HermesException.Network("Failed to connect to Hermes whoami endpoint", e)
        }

        response.use { resp ->
            if (resp.code == 401 || resp.code == 403) {
                throw HermesException.Auth("Invalid or expired API key (HTTP ${resp.code})")
            }
            if (!resp.isSuccessful) {
                throw HermesException.Network("Whoami request failed with HTTP ${resp.code}", code = resp.code)
            }

            val body = resp.body?.string() ?: throw HermesException.Network("Empty response from whoami")
            return parse(body)
        }
    }

    private fun parse(json: String): Identity {
        // Minimal fast zero-dependency JSON extraction for identity fields
        val user = extract(json, "user") ?: ""
        val tenant = extract(json, "tenant") ?: ""
        val tier = extract(json, "tier") ?: ""
        val owner = json.contains("\"owner\":true") || json.contains("\"owner\": true")

        return Identity(
            user = user,
            tenant = tenant,
            owner = owner,
            scopes = emptyList(),
            tier = tier
        )
    }

    private fun extract(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
}
