package pro.aduki.hermes.state.repository

import io.objectbox.BoxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pro.aduki.hermes.store.entities.Mailbox
import pro.aduki.hermes.store.entities.Message
import pro.aduki.hermes.sync.outbox.Manager

/**
 * MailSource abstracts reactive entity streams.
 */
interface MailSource {
    fun mailboxes(): Flow<List<Mailbox>>
    fun messages(mailboxHex: String): Flow<List<Message>>
    fun message(hex: String): Flow<Message?>
    fun getMessage(hex: String): Message?
    fun getMailbox(hex: String): Mailbox?
}

/**
 * Mail repository exposing hot StateFlow reactive query pipelines.
 */
class Mail(
    private val source: MailSource,
    private val manager: Manager? = null,
    private val scope: CoroutineScope
) {

    constructor(store: BoxStore, manager: Manager? = null, scope: CoroutineScope) : this(
        source = object : MailSource {
            private val mailboxBox = store.boxFor(Mailbox::class.java)
            private val messageBox = store.boxFor(Message::class.java)

            override fun mailboxes(): Flow<List<Mailbox>> {
                return MutableStateFlow(mailboxBox.all)
            }

            override fun messages(mailboxHex: String): Flow<List<Message>> {
                return MutableStateFlow(
                    messageBox.all
                        .filter { it.mailbox == mailboxHex }
                        .sortedByDescending { it.date }
                )
            }

            override fun message(hex: String): Flow<Message?> {
                return MutableStateFlow(messageBox.all.firstOrNull { it.hex == hex })
            }

            override fun getMessage(hex: String): Message? {
                return messageBox.all.firstOrNull { it.hex == hex }
            }

            override fun getMailbox(hex: String): Mailbox? {
                return mailboxBox.all.firstOrNull { it.hex == hex }
            }
        },
        manager = manager,
        scope = scope
    )

    /**
     * Hot StateFlow of all mailboxes.
     */
    fun mailboxes(): StateFlow<List<Mailbox>> {
        return source.mailboxes().stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    }

    /**
     * Hot StateFlow of messages for a given mailbox sorted newest first.
     */
    fun messages(mailboxHex: String): StateFlow<List<Message>> {
        return source.messages(mailboxHex).stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    }

    /**
     * Hot StateFlow of a single message by hex.
     */
    fun message(hex: String): StateFlow<Message?> {
        return source.message(hex).stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = source.getMessage(hex)
        )
    }

    /**
     * Hot StateFlow of unread count for a given mailbox.
     */
    fun unread(mailboxHex: String): StateFlow<Int> {
        return source.messages(mailboxHex)
            .map { list -> list.count { !it.seen() } }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = 0
            )
    }

    /**
     * Toggles a flag optimistically via the Outbox Manager.
     */
    fun flag(hex: String, flag: Int) {
        manager?.flag(hex, flag)
    }

    /**
     * Moves a message optimistically via the Outbox Manager.
     */
    fun move(hex: String, dest: String) {
        manager?.move(hex, dest)
    }
}

typealias MailRepository = Mail

