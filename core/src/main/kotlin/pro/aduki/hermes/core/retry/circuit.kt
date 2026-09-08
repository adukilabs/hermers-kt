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
class Circuit(
    private val threshold: Int = 5,
    private val timeout: Long = 15_000
) {
    enum class State {
        CLOSED,s
        OPEN,
        HALF_OPEN
    }

    private val state = AtomicReference(State.CLOSED)
    private val failureCount = AtomicInteger(0)
    private val lastStateChange = AtomicLong(System.currentTimeMillis())
    private val failures = AtomicInteger(0)
    private val changed = AtomicLong(System.currentTimeMillis())

    fun currentState(): State {
    fun state(): State {
        val now = System.currentTimeMillis()
        if (state.get() == State.OPEN && (now - lastStateChange.get()) >= resetTimeoutMs) {
        if (state.get() == State.OPEN && (now - changed.get()) >= timeout) {
            state.compareAndSet(State.OPEN, State.HALF_OPEN)
        }
        return state.get()
    }

    fun onSuccess() {
        failureCount.set(0)
    fun success() {
        failures.set(0)
        state.set(State.CLOSED)
        lastStateChange.set(System.currentTimeMillis())
        changed.set(System.currentTimeMillis())
    }

    fun onFailure() {
        val count = failureCount.incrementAndGet()
        if (count >= failureThreshold || state.get() == State.HALF_OPEN) {
    fun fail() {
        val count = failures.incrementAndGet()
        if (count >= threshold || state.get() == State.HALF_OPEN) {
            state.set(State.OPEN)
            lastStateChange.set(System.currentTimeMillis())
            changed.set(System.currentTimeMillis())
        }
    }

    inline fun <T> execute(block: () -> T): T {
        if (currentState() == State.OPEN) {
        if (state() == State.OPEN) {
            throw HermesException.CircuitOpen()
        }
        return try {
            val result = block()
            onSuccess()
            success()
            result
        } catch (e: Throwable) {
            onFailure()
            fail()
            throw e
        }
    }
}

