package pro.aduki.hermes.net.grpc

import io.grpc.CallCredentials
import io.grpc.Metadata
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executor

class MetadataTest {

    @Test
    fun testCredentialsHeaderInjection() {
        val key = "hm_live_grpc_secret"
        val credentials = Credentials(key)

        var capturedMetadata: Metadata? = null
        val applier = object : CallCredentials.MetadataApplier() {
            override fun apply(headers: Metadata) {
                capturedMetadata = headers
            }

            override fun fail(status: io.grpc.Status) {}
        }

        val directExecutor = Executor { it.run() }
        val info = object : CallCredentials.RequestInfo() {
            override fun getMethodDescriptor(): io.grpc.MethodDescriptor<*, *>? = null
            override fun getSecurityLevel(): io.grpc.SecurityLevel = io.grpc.SecurityLevel.NONE
            override fun getAuthority(): String = "localhost"
            override fun getCallOptions(): io.grpc.CallOptions = io.grpc.CallOptions.DEFAULT
        }
        credentials.applyRequestMetadata(info, directExecutor, applier)


        val authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
        assertEquals("Key $key", capturedMetadata?.get(authKey))
    }
}

