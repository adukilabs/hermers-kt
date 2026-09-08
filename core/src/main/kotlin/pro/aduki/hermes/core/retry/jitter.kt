package pro.aduki.hermes.core.retry

import kotlin.math.min
import kotlin.random.Random

/**
 * Decorrelated Jitter exponential backoff algorithm to prevent client retry synchronization waves.
 */
class DecorrelatedJitter(
    val baseDelayMs: Long = 100,
    val maxDelayMs: Long = 30_000
) {
    private var currentDelay: Long = baseDelayMs

    fun nextDelay(): Long {
        val next = Random.nextLong(baseDelayMs, currentDelay * 3)
        currentDelay = min(maxDelayMs, next)
        return currentDelay
    }

    fun reset() {
        currentDelay = baseDelayMs
    }
}

