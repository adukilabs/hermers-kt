plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":store"))
    implementation(project(":sync"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.coroutines.test)
}

