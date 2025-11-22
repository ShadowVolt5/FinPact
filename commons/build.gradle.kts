plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`          // даёт api(...)
    `maven-publish`         // публикация в mavenLocal/репозиторий
}

group = "ru.finpact"
version = "0.0.1"

repositories { mavenCentral() }

dependencies {
    // Общая утилита/аспектный код часто использует рефлексию
    implementation(libs.kotlin.reflect)

    implementation(libs.ktor.serialization.kotlinx.json)

    // Реэкспортируем DB-стек транзитивно (придёт в auth/payments)
    api(libs.hikaricp)
    api(libs.postgresql)

    // Логирование через API; backend (logback) ставят приложения
    api(libs.slf4j.api)

    // Если в commons есть Ktor-специфичный код (плагины, interceptors) —
    // компилируемся против core, но НЕ тащим его транзитивно
    compileOnly(libs.ktor.server.core)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "ru.finpact"
            artifactId = "commons"
            version = "0.0.1"
        }
    }
    // по умолчанию есть репозиторий mavenLocal()
}
