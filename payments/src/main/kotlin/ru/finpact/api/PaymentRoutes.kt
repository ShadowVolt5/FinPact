package ru.finpact.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.finpact.auth.service.TokenAuthService
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.engine.ContractProxy
import ru.finpact.domain.PaymentService
import ru.finpact.domain.impl.PaymentServiceImpl
import ru.finpact.domain.impl.SubjectExistencePortImpl
import ru.finpact.domain.impl.TokenAuthServiceImpl
import ru.finpact.dto.transfers.CreateTransferRequest
import ru.finpact.dto.transfers.TransferResponse
import ru.finpact.infra.repository.impl.PaymentRepositoryImpl
import ru.finpact.infra.repository.impl.UsersRepositoryImpl

fun Application.paymentRoutes() {
    val usersRepo = UsersRepositoryImpl()
    val subjectPort = SubjectExistencePortImpl(usersRepo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

    val paymentsRepo = PaymentRepositoryImpl()
    val rawPaymentService = PaymentServiceImpl(paymentsRepo)
    val paymentService: PaymentService =
        ContractProxy.wrap<PaymentService>(rawPaymentService)

    routing {
        route("/payments") {
            post("/transfers") {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                    ?: throw ContractViolation("Authorization header must be provided")

                val principal = tokenAuthService.authenticate(authHeader)

                val request = call.receive<CreateTransferRequest>()
                val result: TransferResponse = paymentService.createTransfer(
                    ownerId = principal.userId,
                    request = request,
                )

                call.respond(HttpStatusCode.Created, result)
            }
        }
    }
}
