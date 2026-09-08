plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":crypto"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.stub)
    implementation(libs.json)


    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
