# Error Handling

The SDK encapsulates all failures into a sealed class hierarchy inheriting from `HermesException`.

---

## 1. Exception Hierarchy

```kotlin
sealed class HermesException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class Unauthorized(message: String) : HermesException(message)
    class NotFound(message: String) : HermesException(message)
    class RateLimited(message: String, val retryAfterMs: Long = 0) : HermesException(message)
    class Network(message: String, cause: Throwable? = null) : HermesException(message, cause)
    class Storage(message: String, cause: Throwable? = null) : HermesException(message, cause)
    class Security(message: String, cause: Throwable? = null) : HermesException(message, cause)
    class Unavailable(message: String) : HermesException(message)
}
```

---

## 2. Common Handling Patterns

```kotlin
try {
    hermes.sync.all()
} catch (e: HermesException.Unauthorized) {
    // Session token expired or credentials revoked -> trigger re-login
    navigateToLogin()
} catch (e: HermesException.RateLimited) {
    // Back off for e.retryAfterMs
    delay(e.retryAfterMs)
} catch (e: HermesException.Unavailable) {
    // Circuit breaker is open -> device is offline or server unreachable
    showOfflineBanner()
} catch (e: HermesException) {
    // General SDK error
    Log.e("Hermes", "SDK error: ${e.message}", e)
}
```
