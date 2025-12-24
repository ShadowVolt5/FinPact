package ru.finpact

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import ru.finpact.api.limitsRoutes
import ru.finpact.infra.db.Database
import ru.finpact.infra.http.installErrorHandling

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Database.init(environment.config)

    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls = false
                ignoreUnknownKeys = true
            }
        )
    }

    installErrorHandling()
    limitsRoutes()

    environment.monitor.subscribe(ApplicationStopped) {
        Database.close()
    }
}
