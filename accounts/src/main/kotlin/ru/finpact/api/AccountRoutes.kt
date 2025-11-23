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
import ru.finpact.dto.create.OpenAccountRequest
import ru.finpact.infra.repository.impl.AccountsRepositoryImpl
import ru.finpact.infra.repository.impl.UsersRepositoryImpl

fun Application.accountRoutes() {
    val usersRepo = UsersRepositoryImpl()

    val subjectPort = SubjectExistencePortImpl(usersRepo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

    val accountsRepo = AccountsRepositoryImpl()
    val rawAccountService = AccountServiceImpl(accountsRepo)
    val accountService: AccountService =
        ContractProxy.wrap<AccountService>(rawAccountService)

    routing {
        route("/accounts") {
            post {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                    ?: throw ContractViolation("Authorization header must be provided")

                val principal = tokenAuthService.authenticate(authHeader)

                val request = call.receive<OpenAccountRequest>()

                val result = accountService.openAccount(
                    ownerId = principal.userId,
                    request = request,
                )

                call.respond(HttpStatusCode.Created, result)
            }

            get("/{id}") {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                    ?: throw ContractViolation("Authorization header must be provided")

                val principal = tokenAuthService.authenticate(authHeader)

                val idParam = call.parameters["id"]
                    ?: throw ContractViolation("account id must be provided")

                val accountId = idParam.toLongOrNull()
                    ?: throw ContractViolation("account id must be a number")

                val result: AccountResponse = accountService.getAccount(
                    ownerId = principal.userId,
                    accountId = accountId,
                )

                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
