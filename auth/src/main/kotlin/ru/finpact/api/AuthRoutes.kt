package ru.finpact.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.finpact.auth.service.TokenAuthService
import ru.finpact.contracts.engine.ContractProxy
import ru.finpact.domain.AuthService
import ru.finpact.domain.impl.AuthServiceImpl
import ru.finpact.domain.impl.SubjectExistencePortImpl
import ru.finpact.domain.impl.TokenAuthServiceImpl
import ru.finpact.dto.login.LoginRequest
import ru.finpact.dto.login.LoginResponse
import ru.finpact.dto.register.RegisterRequest
import ru.finpact.dto.register.RegisterResponse
import ru.finpact.infra.repository.impl.AuthRepositoryImpl

fun Application.authRoutes() {
    val repo = AuthRepositoryImpl()

    val rawAuthService = AuthServiceImpl(repo)
    val service: AuthService = ContractProxy.wrap<AuthService>(rawAuthService)

    val subjectPort = SubjectExistencePortImpl(repo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

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
