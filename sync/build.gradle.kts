plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":store"))
    implementation(project(":net"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.objectbox.kotlin)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.coroutines.test)
}

