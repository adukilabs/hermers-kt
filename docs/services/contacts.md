# Contacts Service

The `hermes.contacts` service manages address book contacts, incremental `ctag` synchronization, and instant incremental search.

---

## 1. Observing Contacts

```kotlin
// Hot StateFlow emitting all contacts sorted alphabetically by name
val contacts: StateFlow<List<Contact>>? = hermes.contacts.observe()
```

---

## 2. Fast Incremental Search

```kotlin
// Reactive search filtered by name, email, or phone
val searchResults: StateFlow<List<Contact>>? = hermes.contacts.search("alice")
```

Search operations execute over memory-mapped ObjectBox indices, providing lag-free autocomplete as the user types.

---

## 3. Synchronous Fast Lookup

```kotlin
// Lookup single contact by hex
val contact = hermes.contacts.get("c_01HZ9...")
```

---

## 4. Manual Delta Sync

```kotlin
viewModelScope.launch {
    val success = hermes.contacts.sync()
    if (success) {
        Log.d("Hermes", "Contacts synchronized")
    }
}
```
