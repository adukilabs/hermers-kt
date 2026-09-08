plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":crypto"))
    implementation(project(":state"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.stub)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
