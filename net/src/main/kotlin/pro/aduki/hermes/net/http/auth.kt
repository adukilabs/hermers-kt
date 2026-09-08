package pro.aduki.hermes.net.http

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor adding API key authentication header to all outgoing Hermes REST requests.
 */
class AuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Key $apiKey")
            .header("Accept", "application/json")
            .header("User-Agent", "Hermes-Android/1.0.0")
            .build()
        return chain.proceed(request)
    }
}

