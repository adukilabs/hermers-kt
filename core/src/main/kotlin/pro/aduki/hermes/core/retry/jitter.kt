package pro.aduki.hermes.core.retry

import kotlin.math.min
import kotlin.random.Random

/**
 * Decorrelated Jitter exponential backoff algorithm to prevent client retry synchronization waves.
 */
class Jitter(
    val base: Long = 100,
    val max: Long = 30_000,
    private val random: Random = Random.Default
) {
    private var delay: Long = base

    /**
     * Calculates the next backoff delay.
     */
    fun next(attempt: Int = 0): Long {
        val bound = if (delay * 3 > base) delay * 3 else base + 1
        val next = random.nextLong(base, bound)
        delay = min(max, next)
        return delay
    }

    /**
     * Resets delay to base.
     */
    fun reset() {
        delay = base
    }

    /**
     * Current delay value.
     */
    fun current(): Long = delay
}

/**
 * Typealias for DecorrelatedJitter.
 */
typealias DecorrelatedJitter = Jitter
