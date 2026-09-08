# API Keys

API keys are intended for automated background processes, IoT devices, or headless services that interact with Hermes without human credential entry.

---

## 1. Key Format & Scopes

Hermes API keys are cryptographic identifiers prefixed with `hm_live_` (production) or `hm_test_` (sandbox).

```text
hm_live_7f9b8c2d1e0a4b5c6d7e8f9a0b1c2d3e
```

API keys inherit explicitly granted permission scopes (e.g. `mail:read`, `mail:send`, `contacts:read`), or full access if issued by a tenant owner.

---

## 2. Initializing with an API Key

Pass the key directly to `HermesClient.builder()`:

```kotlin
val client = HermesClient.builder()
    .key("hm_live_7f9b8c2d1e0a4b5c6d7e8f9a0b1c2d3e")
    .endpoint("https://hermers.aduki.pro/v1")
    .build()
```

The SDK automatically injects:

- REST: `Authorization: Key hm_live_...`
- gRPC: Metadata key `authorization: Key hm_live_...`
