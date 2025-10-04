package ru.finpact

import io.ktor.server.application.*
import ru.finpact.infra.db.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Database.init(environment.config)
    configureRouting()

    environment.monitor.subscribe(ApplicationStopped) {
        Database.close()
    }
}
