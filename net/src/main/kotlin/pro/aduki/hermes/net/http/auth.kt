package pro.aduki.hermes.net.http

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Auth interceptor attaching API key and client metadata to all HTTP requests.
 */
class Auth(private val key: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Key $key")
            .header("Accept", "application/json")
            .header("User-Agent", "Hermes-Android/1.0.0")
            .build()
        return chain.proceed(request)
    }
}
