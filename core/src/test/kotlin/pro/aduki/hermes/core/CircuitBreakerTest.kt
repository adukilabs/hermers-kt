package pro.aduki.hermes.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import pro.aduki.hermes.core.errors.HermesException
import pro.aduki.hermes.core.retry.CircuitBreaker

class CircuitBreakerTest {

    @Test
    fun testTripsToOpenAfterThresholdFailures() {
        val breaker = CircuitBreaker(failureThreshold = 3, resetTimeoutMs = 1000)
        assertEquals(CircuitBreaker.State.CLOSED, breaker.currentState())

        breaker.onFailure()
        breaker.onFailure()
        assertEquals(CircuitBreaker.State.CLOSED, breaker.currentState())

        breaker.onFailure() // 3rd failure
        assertEquals(CircuitBreaker.State.OPEN, breaker.currentState())

        assertThrows(HermesException.CircuitOpen::class.java) {
            breaker.execute { "should fail fast" }
        }
    }

    @Test
    fun testRecoversOnSuccess() {
        val breaker = CircuitBreaker(failureThreshold = 2, resetTimeoutMs = 1000)
        breaker.onFailure()
        breaker.onSuccess()
        assertEquals(CircuitBreaker.State.CLOSED, breaker.currentState())
    }
}

