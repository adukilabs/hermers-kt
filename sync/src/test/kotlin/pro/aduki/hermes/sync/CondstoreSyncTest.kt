package pro.aduki.hermes.sync

import org.junit.Assert.assertEquals
import org.junit.Test
import pro.aduki.hermes.store.entities.Message
import pro.aduki.hermes.sync.reconcile.Reconcile

class CondstoreSyncTest {

    @Test
    fun testServerFlagsOverrideCleanLocalMessage() {
        val local = Message(
            hex = "msg_1",
            flags = Message.FLAG_SEEN,
            dirty = false
        )
        val serverFlags = Message.FLAG_SEEN or Message.FLAG_FLAGGED

        val merged = Reconcile.mergeFlags(local, serverFlags)
        assertEquals(serverFlags, merged)
    }

    @Test
    fun testDirtyLocalMessagePreservesLocalFlags() {
        val local = Message(
            hex = "msg_2",
            flags = Message.FLAG_FLAGGED,
            dirty = true
        )
        val serverFlags = 0 // Server thinks it is not flagged

        val merged = Reconcile.mergeFlags(local, serverFlags)
        assertEquals(Message.FLAG_FLAGGED, merged)
    }
}

