package pro.aduki.hermes.sync.reconcile

import org.junit.Assert.assertEquals
import org.junit.Test
import pro.aduki.hermes.store.entities.Message

class MergeTest {

    @Test
    fun testServerOverride() {
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
    fun testLocalPreserve() {
        val local = Message(
            hex = "msg_2",
            flags = Message.FLAG_FLAGGED,
            dirty = true
        )
        val serverFlags = 0

        val merged = Reconcile.mergeFlags(local, serverFlags)
        assertEquals(Message.FLAG_FLAGGED, merged)
    }
}

