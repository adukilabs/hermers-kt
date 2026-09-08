# Dual Network Transports

The Hermes Android SDK provides two transport pipelines tailored for low latency and high bandwidth efficiency.

---

## 1. OkHttp 4 HTTP/2 REST Pipeline

- **Protocol**: HTTP/2 multiplexing over single TCP/TLS connection.
- **Connection Pool**: 5 idle connections, 5-minute keep-alive TTL.
- **Interceptors**:
  - `Auth`: Dynamic header injector (`Key <apiKey>` or `Bearer <jwt>`).
  - Gzip / Deflate payload compression.

```kotlin
val okHttpClient = Client.create(
    auth = "hm_live_...",
    timeoutSeconds = 15
)
```

---

## 2. gRPC OkHttp Channel Pipeline

For binary sync streams and high-throughput payloads:
- **Client**: `grpc-okhttp` (version 1.64.0).
- **Security**: TLS with ALPN negotiation.
- **Metadata**: `KeyCallCredentials` attaches authorization headers to every unary and streaming RPC call without connection teardown.

```kotlin
val channel = Channel.build(
    host = "grpc.aduki.pro",
    port = 443
)
```
