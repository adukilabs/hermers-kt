package pro.aduki.hermes.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pro.aduki.hermes.store.entities.Message

class MessageEntityTest {

    @Test
    fun testFlagBitmaskOperations() {
        val msg = Message(
            hex = "msg_001",
            mailbox = "inbox",
            uid = 100,
            subject = "Fast Kotlin Android SDK",
            flags = 0
        )

        assertFalse(msg.isSeen())
        assertFalse(msg.isFlagged())

        // Mark seen
        msg.toggleFlag(Message.FLAG_SEEN)
        assertTrue(msg.isSeen())
        assertTrue(msg.dirty)

        // Mark starred / flagged
        msg.toggleFlag(Message.FLAG_FLAGGED)
        assertTrue(msg.isFlagged())
        assertTrue(msg.isSeen())

        // Toggle seen off
        msg.toggleFlag(Message.FLAG_SEEN)
        assertFalse(msg.isSeen())
        assertTrue(msg.isFlagged())
    }

    @Test
    fun testRecipientDelimiterPacking() {
        val msg = Message(
            to = "alice@example.com,bob@example.com"
        )
        val recipients = msg.to.split(",")
        assertEquals(2, recipients.size)
        assertEquals("alice@example.com", recipients[0])
    }
}

