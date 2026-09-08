# Installation

This guide walks you through integrating the Hermes Android Kotlin SDK into your Android application using Gradle Kotlin DSL (`.gradle.kts`).

---

## 1. Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer.
- **Minimum SDK**: Android API 26 (Android 8.0 Oreo) or higher.
- **Target SDK**: Android API 34+ (Android 14).
- **Kotlin Version**: 1.9.20+ or 2.0.0+.
- **ObjectBox Version**: 4.0.3.

---

## 2. Configure Repositories

Ensure `google()` and `mavenCentral()` are declared in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

---

## 3. Apply the ObjectBox Plugin

In your project-level `build.gradle.kts`:

```kotlin
plugins {
    id("io.objectbox") version "4.0.3" apply false
}
```

In your application module `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("io.objectbox") // Generates FlatBuffers entity bindings at compile time
}
```

---

## 4. Add Dependencies

Add the Hermes SDK and related runtime libraries to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Hermes Android SDK Facade
    implementation("pro.aduki.hermes:sdk:1.0.0")

    // ObjectBox Zero-Copy Persistent Engine
    implementation("io.objectbox:objectbox-kotlin:4.0.3")
    implementation("io.objectbox:objectbox-android:4.0.3")

    // Network & gRPC Transports
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.grpc:grpc-okhttp:1.64.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
```

---

## 5. ProGuard / R8 Rules

ObjectBox and OkHttp require minimal ProGuard configuration. Add the following to `app/proguard-rules.pro`:

```proguard
# ObjectBox FlatBuffers Bytecode Preservation
-keepclassmembers class * {
    @io.objectbox.annotation.Entity <fields>;
}
-keep class io.objectbox.** { *; }
-dontwarn io.objectbox.**

# OkHttp 4 Rules
-dontwarn okhttp3.**
-dontwarn okio.**
```
