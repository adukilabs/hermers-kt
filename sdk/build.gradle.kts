plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core"))
    api(project(":crypto"))
    api(project(":store"))
    api(project(":net"))
    api(project(":sync"))
    api(project(":state"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)

    testImplementation(libs.junit)
}

