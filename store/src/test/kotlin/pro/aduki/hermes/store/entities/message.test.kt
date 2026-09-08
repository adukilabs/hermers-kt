package pro.aduki.hermes.store.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTest {

    @Test
    fun testFlags() {
        val msg = Message(
            hex = "msg_001",
            mailbox = "inbox",
            uid = 100,
            subject = "Fast Kotlin Android SDK",
            flags = 0
        )

        assertFalse(msg.seen())
        assertFalse(msg.flagged())

        // Mark seen
        msg.toggle(Message.SEEN)
        assertTrue(msg.seen())
        assertTrue(msg.dirty)

        // Mark flagged
        msg.toggle(Message.FLAGGED)
        assertTrue(msg.flagged())
        assertTrue(msg.seen())

        // Toggle seen off
        msg.toggle(Message.SEEN)
        assertFalse(msg.seen())
        assertTrue(msg.flagged())
    }

    @Test
    fun testRecipients() {
        val msg = Message(
            to = "alice@example.com, bob@example.com"
        )
        val recipients = msg.recipients()
        assertEquals(2, recipients.size)
        assertEquals("alice@example.com", recipients[0])
        assertEquals("bob@example.com", recipients[1])
    }
}
