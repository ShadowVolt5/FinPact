plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-library`
}

group = "ru.finpact"
version = "0.0.1"

repositories { mavenCentral() }

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.ktor.serialization.kotlinx.json)

    api(libs.hikaricp)
    api(libs.postgresql)
    api(libs.slf4j.api)
    api(libs.flyway.core)
    api(libs.flyway.postgres)

    compileOnly(libs.ktor.server.core)
}

java {
    withSourcesJar()
    withJavadocJar()
}
