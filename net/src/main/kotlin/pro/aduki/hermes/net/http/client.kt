package pro.aduki.hermes.net.http

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Factory for building high-performance OkHttpClient with connection pooling and timeouts.
 */
object HttpClientFactory {

    fun create(apiKey: String, timeoutSeconds: Long = 15): OkHttpClient {
        return OkHttpClient.Builder()
            .connectionPool(ConnectionPool(maxIdleConnections = 8, keepAliveDuration = 5, TimeUnit.MINUTES))
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(apiKey))
            .retryOnConnectionFailure(true)
            .build()
    }
}

