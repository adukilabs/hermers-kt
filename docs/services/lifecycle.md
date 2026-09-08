# Lifecycle Management

Mobile battery life depends heavily on ceasing background network polling while the app is inactive. The `Lifecycle` coordinator bridges Android lifecycle events to the SDK sync engine.

---

## 1. Foreground & Background Transitions

```kotlin
// In your Android Application, ActivityLifecycleCallbacks, or ProcessLifecycleOwner:

override fun onStop() {
    // App backgrounded: pause background polling
    hermes.pause()
}

override fun onStart() {
    // App foregrounded: resume polling and flush pending outbox actions
    hermes.resume()
}
```

---

## 2. Automatic Outbox Flush on Resume

When `hermes.resume()` is called:
1. Active status switches to `true`.
2. Registered lifecycle listeners fire.
3. An immediate outbox drain is initiated in a background coroutine, dispatching all offline mutations created while the device was disconnected.
