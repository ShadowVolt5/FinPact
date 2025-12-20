plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "ru.finpact"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.finpact.commons)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
