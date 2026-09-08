plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    id("io.objectbox") version "4.0.3" apply false
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }
}

