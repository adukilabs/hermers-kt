package pro.aduki.hermes.net.grpc

import io.grpc.CallCredentials
import io.grpc.Metadata
import java.util.concurrent.Executor

/**
 * KeyCallCredentials attaches API key metadata to gRPC service invocations.
 */
class KeyCallCredentials(private val apiKey: String) : CallCredentials() {
    private val authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)

    override fun applyRequestMetadata(
        requestInfo: RequestInfo,
        appExecutor: Executor,
        applier: MetadataApplier
    ) {
        appExecutor.execute {
            try {
                val headers = Metadata()
                headers.put(authKey, "Key $apiKey")
                applier.apply(headers)
            } catch (t: Throwable) {
                applier.fail(io.grpc.Status.UNAUTHENTICATED.withCause(t))
            }
        }
    }

    override fun thisUsesUnstableApi() {}
}

