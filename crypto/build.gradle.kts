plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.junit)
}

