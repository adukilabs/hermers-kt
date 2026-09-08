package pro.aduki.hermes.core.retry

import pro.aduki.hermes.core.errors.HermesException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Three-state circuit breaker to prevent radio battery drain during network outages.
 */
class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 15_000
) {
    enum class State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private val state = AtomicReference(State.CLOSED)
    private val failureCount = AtomicInteger(0)
    private val lastStateChange = AtomicLong(System.currentTimeMillis())

    fun currentState(): State {
        val now = System.currentTimeMillis()
        if (state.get() == State.OPEN && (now - lastStateChange.get()) >= resetTimeoutMs) {
            state.compareAndSet(State.OPEN, State.HALF_OPEN)
        }
        return state.get()
    }

    fun onSuccess() {
        failureCount.set(0)
        state.set(State.CLOSED)
        lastStateChange.set(System.currentTimeMillis())
    }

    fun onFailure() {
        val count = failureCount.incrementAndGet()
        if (count >= failureThreshold || state.get() == State.HALF_OPEN) {
            state.set(State.OPEN)
            lastStateChange.set(System.currentTimeMillis())
        }
    }

    inline fun <T> execute(block: () -> T): T {
        if (currentState() == State.OPEN) {
            throw HermesException.CircuitOpen()
        }
        return try {
            val result = block()
            onSuccess()
            result
        } catch (e: Throwable) {
            onFailure()
            throw e
        }
    }
}

