package ru.finpact.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.finpact.contracts.engine.ContractProxy
import ru.finpact.domain.AuthService
import ru.finpact.domain.impl.AuthServiceImpl
import ru.finpact.dto.login.LoginRequest
import ru.finpact.dto.login.LoginResponse
import ru.finpact.dto.register.RegisterRequest
import ru.finpact.dto.register.RegisterResponse
import ru.finpact.infra.repository.impl.AuthRepositoryImpl

fun Application.authRoutes() {
    val repo = AuthRepositoryImpl()
    val rawService = AuthServiceImpl(repo)
    val service: AuthService = ContractProxy.wrap<AuthService>(rawService)

    routing {
        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val result: RegisterResponse = service.register(request)
                call.respond(HttpStatusCode.Created, result)
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val result: LoginResponse = service.login(request)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
