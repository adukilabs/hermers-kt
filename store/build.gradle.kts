plugins {
    kotlin("jvm")
    id("io.objectbox")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":crypto"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.objectbox.kotlin)

    testImplementation(libs.junit)
}

