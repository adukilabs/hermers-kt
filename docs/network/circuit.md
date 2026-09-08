# Circuit Breaker & Fault Tolerance

To protect battery life and avoid hammering unreachable backend endpoints when the device encounters poor network coverage, the SDK integrates an atomic 3-state `Circuit` breaker.

---

## 1. State Machine

```text
       ┌─────────── 5 Consecutive Failures ───────────┐
       ▼                                              │
  ┌─────────┐                                   ┌──────────┐
  │ CLOSED  │                                   │   OPEN   │
  └─────────┘                                   └──────────┘
       ▲                                              │
       │                                              ▼
       └────────── 1 Success ──────────── 15s Reset Timeout (HALF-OPEN)
```

1. **CLOSED**: Normal operation. All requests pass through.
2. **OPEN**: Trips after 5 consecutive failures. Subsequent requests fail-fast immediately without radio wakeups.
3. **HALF-OPEN**: After a 15-second probe delay, allows a single canary request. Success resets to `CLOSED`; failure returns to `OPEN`.

---

## 2. Usage

In `core/retry/circuit.kt`:

```kotlin
val circuit = Circuit(threshold = 5, timeout = 15_000)

try {
    val response = circuit.execute {
        transport.send(payload)
    }
} catch (e: HermesException.Unavailable) {
    // Fast-fail: Circuit is currently OPEN
}
```
