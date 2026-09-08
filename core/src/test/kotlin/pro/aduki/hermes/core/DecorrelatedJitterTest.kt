package pro.aduki.hermes.core

import org.junit.Assert.assertTrue
import org.junit.Test
import pro.aduki.hermes.core.retry.DecorrelatedJitter

class DecorrelatedJitterTest {

    @Test
    fun testDelayWithinBounds() {
        val jitter = DecorrelatedJitter(baseDelayMs = 50, maxDelayMs = 5000)
        for (i in 1..100) {
            val delay = jitter.nextDelay()
            assertTrue("Delay $delay was less than base 50", delay >= 50)
            assertTrue("Delay $delay exceeded max 5000", delay <= 5000)
        }
    }

    @Test
    fun testResetRestoresBase() {
        val jitter = DecorrelatedJitter(baseDelayMs = 100, maxDelayMs = 10_000)
        repeat(10) { jitter.nextDelay() }
        jitter.reset()
        val firstAfterReset = jitter.nextDelay()
        assertTrue(firstAfterReset in 100..300)
    }
}

