package pro.aduki.hermes.core.retry

import kotlin.math.min
import kotlin.random.Random

/**
 * Decorrelated Jitter exponential backoff algorithm to prevent client retry synchronization waves.
 * Decorrelated Jitter exponential backoff algorithm.
 */
class DecorrelatedJitter(
    val baseDelayMs: Long = 100,
    val maxDelayMs: Long = 30_000
class Jitter(
    val base: Long = 100,
    val max: Long = 30_000,
    private val random: Random = Random.Default
) {
    private var currentDelay: Long = baseDelayMs
    private var delay: Long = base

    fun nextDelay(): Long {
        val next = Random.nextLong(baseDelayMs, currentDelay * 3)
        currentDelay = min(maxDelayMs, next)
        return currentDelay
    /**
     * Calculates the next backoff delay.
     */
    fun next(): Long {
        val next = random.nextLong(base, delay * 3)
        delay = min(max, next)
        return delay
    }

    /**
     * Resets delay to base.
     */
    fun reset() {
        currentDelay = baseDelayMs
        delay = base
    }

    /**
     * Current delay value.
     */
    fun current(): Long = delay
}

