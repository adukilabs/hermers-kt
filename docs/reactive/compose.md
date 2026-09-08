# Jetpack Compose UI Binding

Because the SDK exposes standard Kotlin `StateFlow` primitives, integrating with Jetpack Compose requires zero adapter boilerplate.

---

## 1. ViewModel Implementation

```kotlin
class MailboxViewModel(
    private val hermes: HermesClient,
    private val mailboxHex: String = "inbox"
) : ViewModel() {

    // Hot StateFlow collecting directly from ObjectBox observer
    val messages: StateFlow<List<Message>> = hermes.mail
        .observe(mailboxHex) ?: MutableStateFlow(emptyList())

    val unreadCount: StateFlow<Int> = hermes.mail
        .unread(mailboxHex) ?: MutableStateFlow(0)

    fun onToggleFlag(messageHex: String) {
        viewModelScope.launch {
            hermes.mail.flag(messageHex, Message.FLAGGED)
        }
    }

    fun onMarkSeen(messageHex: String) {
        viewModelScope.launch {
            hermes.mail.flag(messageHex, Message.SEEN)
        }
    }
}
```

---

## 2. Composable Screen

```kotlin
@Composable
fun MailboxScreen(viewModel: MailboxViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val unread by viewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inbox ($unread unread)") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(messages, key = { it.hex }) { msg ->
                MessageRow(
                    message = msg,
                    onToggleFlag = { viewModel.onToggleFlag(msg.hex) },
                    onOpen = { viewModel.onMarkSeen(msg.hex) }
                )
            }
        }
    }
}
```
