# Configuration

The Hermes Android SDK is configured and instantiated via `HermesClient.builder()`.

---

## 1. Builder Options

| Method | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `key(string)` | String | `""` | Static API key (`hm_live_...`) for headless/daemon use. |
| `token(string)` | String | `""` | Active JWT access token for user sessions. |
| `endpoint(url)` | String | `https://hermers.aduki.pro/v1` | Base REST API URL. |
| `grpc(host, port)` | Host, Port | `grpc.aduki.pro`, `443` | Target gRPC binary transport host & port. |
| `timeout(seconds)` | Long | `15` | Network call timeout in seconds. |
| `secure(boolean)` | Boolean | `true` | Enables hardware KeyStore encrypted ObjectBox storage. |

---

## 2. Basic Initialization

### Headless / Background Service Configuration
```kotlin
val hermes = HermesClient.builder()
    .key("hm_live_7f9b8c2d1e0a4b5c6d7e8f9a0b1c2d3e")
    .endpoint("https://hermers.aduki.pro/v1")
    .grpc("grpc.aduki.pro", 443)
    .timeout(30)
    .secure(true)
    .build()
```

### Self-Hosted / Local Development Setup
```kotlin
val hermes = HermesClient.builder()
    .key("hm_test_development_key")
    .endpoint("http://10.0.2.2:8080/v1") // Android Emulator host loopback
    .grpc("10.0.2.2", 8443)
    .secure(false) // Software fallback for local testing
    .build()
```

---

## 3. Dependency Injection (Hilt / Koin)

Example Hilt module configuration:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HermesModule {

    @Provides
    @Singleton
    fun provideHermesClient(@ApplicationContext context: Context): HermesClient {
        return HermesClient.builder()
            .key(BuildConfig.HERMES_API_KEY)
            .endpoint(BuildConfig.HERMES_ENDPOINT)
            .secure(true)
            .build()
    }
}
```
