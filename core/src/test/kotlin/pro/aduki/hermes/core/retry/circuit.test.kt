package pro.aduki.hermes.core.retry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import pro.aduki.hermes.core.errors.HermesException

class CircuitTest {

    @Test
    fun testTripsToOpen() {
        val circuit = Circuit(threshold = 3, timeout = 1000)
        assertEquals(Circuit.State.CLOSED, circuit.state())

        circuit.fail()
        circuit.fail()
        assertEquals(Circuit.State.CLOSED, circuit.state())

        circuit.fail() // 3rd failure
        assertEquals(Circuit.State.OPEN, circuit.state())

        assertThrows(HermesException.CircuitOpen::class.java) {
            circuit.execute { "fail fast" }
        }
    }

    @Test
    fun testSuccessResets() {
        val circuit = Circuit(threshold = 2, timeout = 1000)
        circuit.fail()
        circuit.success()
        assertEquals(Circuit.State.CLOSED, circuit.state())
    }
}
