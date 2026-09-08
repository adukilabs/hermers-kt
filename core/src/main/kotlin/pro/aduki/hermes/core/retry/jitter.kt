package pro.aduki.hermes.core.retry

import kotlin.math.min
import kotlin.random.Random

/**
 * Decorrelated Jitter exponential backoff algorithm.
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
    fun next(): Long {
        val next = random.nextLong(base, delay * 3)
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
