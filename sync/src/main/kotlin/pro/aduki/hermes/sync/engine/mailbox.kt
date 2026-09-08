package pro.aduki.hermes.sync.engine

import io.objectbox.BoxStore
import pro.aduki.hermes.store.entities.Mailbox
import pro.aduki.hermes.store.entities.Message
import pro.aduki.hermes.sync.reconcile.Reconcile

/**
 * MailboxDelta models incremental CONDSTORE/MODSEQ updates.
 */
data class MailboxDelta(
    val newUids: List<Long> = emptyList(),
    val changedUids: List<Long> = emptyList(),
    val removedUids: List<Long> = emptyList(),
    val modseq: Long = 0L,
    val uidvalidity: Long = 0L,
    val messages: List<Message> = emptyList()
)

/**
 * MailboxTransport defines network transport for CONDSTORE sync.
 */
fun interface MailboxTransport {
    suspend fun fetch(mailbox: String, uidvalidity: Long, modseq: Long): MailboxDelta
}

/**
 * MailboxStorage abstracts persistence for CONDSTORE sync.
 */
interface MailboxStorage {
    fun getMailbox(hex: String): Mailbox?
    fun putMailbox(mailbox: Mailbox)
    fun getMessages(mailboxHex: String): List<Message>
    fun putMessages(messages: List<Message>)
    fun removeMessages(messages: List<Message>)
    fun clearMailbox(mailboxHex: String)
    fun <T> tx(block: () -> T): T
}

/**
 * Mailbox synchronizer implementing RFC 7162 CONDSTORE / MODSEQ delta synchronization.
 */
class Mailbox(
    private val storage: MailboxStorage,
    private val transport: MailboxTransport
) {

    constructor(store: BoxStore, transport: MailboxTransport) : this(object : MailboxStorage {
        private val mailboxes = store.boxFor(Mailbox::class.java)
        private val messages = store.boxFor(Message::class.java)

        override fun getMailbox(hex: String): Mailbox? = mailboxes.all.firstOrNull { it.hex == hex }
        override fun putMailbox(mailbox: Mailbox) { mailboxes.put(mailbox) }
        override fun getMessages(mailboxHex: String): List<Message> = messages.all.filter { it.mailbox == mailboxHex }
        override fun putMessages(messages: List<Message>) { this.messages.put(messages) }
        override fun removeMessages(messages: List<Message>) { this.messages.remove(messages) }
        override fun clearMailbox(mailboxHex: String) {
            val toDelete = messages.all.filter { it.mailbox == mailboxHex }
            messages.remove(toDelete)
        }
        override fun <T> tx(block: () -> T): T = store.callInTx(block)
    }, transport)

    /**
     * Performs incremental delta sync for the given mailbox.
     */
    suspend fun sync(mailboxHex: String): Boolean {
        val mailbox = storage.getMailbox(mailboxHex) ?: return false

        val delta = transport.fetch(
            mailbox = mailboxHex,
            uidvalidity = mailbox.uidvalidity,
            modseq = mailbox.modseq
        )

        return storage.tx {
            // 1. UIDVALIDITY Mismatch Check (RFC 7162 Invalidation)
            if (delta.uidvalidity != mailbox.uidvalidity) {
                storage.clearMailbox(mailboxHex)
                mailbox.uidvalidity = delta.uidvalidity
                mailbox.modseq = delta.modseq
                storage.putMailbox(mailbox)
                if (delta.messages.isNotEmpty()) {
                    storage.putMessages(delta.messages)
                }
                return@tx true
            }

            // 2. Remove purged UIDs
            val current = storage.getMessages(mailboxHex)
            if (delta.removedUids.isNotEmpty()) {
                val toRemove = current.filter { it.uid in delta.removedUids }
                storage.removeMessages(toRemove)
            }

            // 3. Apply updates to changed UIDs with conflict resolution
            val changedMap = delta.messages.associateBy { it.uid }
            val toUpdate = mutableListOf<Message>()
            for (localMsg in current) {
                if (localMsg.uid in delta.changedUids) {
                    val serverMsg = changedMap[localMsg.uid]
                    if (serverMsg != null) {
                        val finalFlags = Reconcile.flags(localMsg, serverMsg.flags)
                        val finalMailbox = Reconcile.mailbox(localMsg, serverMsg.mailbox)
                        toUpdate.add(localMsg.copy(flags = finalFlags, mailbox = finalMailbox))
                    }
                }
            }
            if (toUpdate.isNotEmpty()) {
                storage.putMessages(toUpdate)
            }

            // 4. Insert newly discovered UIDs
            val newMsgs = delta.messages.filter { it.uid in delta.newUids }
            if (newMsgs.isNotEmpty()) {
                storage.putMessages(newMsgs)
            }

            // 5. Advance mailbox modseq
            mailbox.modseq = delta.modseq
            storage.putMailbox(mailbox)
            true
        }
    }
}
