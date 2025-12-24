package ru.finpact.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.finpact.auth.service.TokenAuthService
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.engine.ContractProxy
import ru.finpact.domain.LimitsService
import ru.finpact.domain.impl.LimitsServiceImpl
import ru.finpact.domain.impl.SubjectExistencePortImpl
import ru.finpact.domain.impl.TokenAuthServiceImpl
import ru.finpact.dto.limits.LimitsProfileResponse
import ru.finpact.dto.limits.LimitsUsageResponse
import ru.finpact.infra.repository.impl.LimitsRepositoryImpl
import ru.finpact.infra.repository.impl.UsersRepositoryImpl

fun Application.limitsRoutes() {
    val usersRepo = UsersRepositoryImpl()
    val subjectPort = SubjectExistencePortImpl(usersRepo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

    val limitsRepo = LimitsRepositoryImpl()
    val rawLimitsService = LimitsServiceImpl(limitsRepo)
    val limitsService: LimitsService =
        ContractProxy.wrap<LimitsService>(rawLimitsService)

    routing {
        route("/limits") {

            get("/{ownerId}") {
                val principal = call.requirePrincipal(tokenAuthService)
                val ownerId = call.requirePathLong("ownerId", "owner id")

                val result: LimitsProfileResponse = limitsService.getLimitsProfile(
                    ownerId = ownerId,
                    callerId = principal.userId,
                )

                call.respond(HttpStatusCode.OK, result)
            }

            get("/usage") {
                val principal = call.requirePrincipal(tokenAuthService)
                val ownerId = call.requirePathLong("ownerId", "owner id")

                val result: LimitsUsageResponse = limitsService.getLimitsUsage(
                    ownerId = ownerId,
                    callerId = principal.userId,
                )

                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}

private fun ApplicationCall.requirePrincipal(tokenAuthService: TokenAuthService) =
    tokenAuthService.authenticate(
        request.headers[HttpHeaders.Authorization]
            ?: throw ContractViolation.unauthorized("Authorization header must be provided")
    )

private fun ApplicationCall.requirePathLong(paramName: String, humanName: String): Long {
    val raw = parameters[paramName]
        ?: throw ContractViolation.badRequest("$humanName must be provided")

    return raw.toLongOrNull()
        ?: throw ContractViolation.badRequest("$humanName must be a number")
}
