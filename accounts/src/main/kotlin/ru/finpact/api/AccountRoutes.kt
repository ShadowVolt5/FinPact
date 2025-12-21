package ru.finpact.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.finpact.auth.service.TokenAuthService
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.engine.ContractProxy
import ru.finpact.domain.AccountService
import ru.finpact.domain.impl.AccountServiceImpl
import ru.finpact.domain.impl.SubjectExistencePortImpl
import ru.finpact.domain.impl.TokenAuthServiceImpl
import ru.finpact.dto.common.AccountResponse
import ru.finpact.dto.created.OpenAccountRequest
import ru.finpact.dto.deposits.DepositRequest
import ru.finpact.infra.repository.impl.AccountRepositoryImpl
import ru.finpact.infra.repository.impl.UsersRepositoryImpl

fun Application.accountRoutes() {
    val usersRepo = UsersRepositoryImpl()
    val subjectPort = SubjectExistencePortImpl(usersRepo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

    val accountsRepo = AccountRepositoryImpl()
    val rawAccountService = AccountServiceImpl(accountsRepo)
    val accountService: AccountService =
        ContractProxy.wrap<AccountService>(rawAccountService)

    routing {
        route("/accounts") {

            post {
                val principal = call.requirePrincipal(tokenAuthService)
                val request = call.receive<OpenAccountRequest>()

                val result = accountService.openAccount(
                    ownerId = principal.userId,
                    request = request,
                )

                call.respond(HttpStatusCode.Created, result)
            }

            get("/{accountId}") {
                val principal = call.requirePrincipal(tokenAuthService)
                val accountId = call.requirePathLong("accountId", "account id")

                val result: AccountResponse = accountService.getAccount(
                    accountId = accountId,
                    ownerId = principal.userId,
                )

                call.respond(HttpStatusCode.OK, result)
            }

            post("/{accountId}/deposits") {
                val principal = call.requirePrincipal(tokenAuthService)
                val accountId = call.requirePathLong("accountId", "account id")
                val request = call.receive<DepositRequest>()

                val result: AccountResponse = accountService.deposit(
                    ownerId = principal.userId,
                    accountId = accountId,
                    request = request,
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
