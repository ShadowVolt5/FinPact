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
import ru.finpact.dto.gettransfers.PaymentDetailsResponse
import ru.finpact.dto.searchpayments.PaymentsSearchRequest
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

            get("/{paymentId}") {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                    ?: throw ContractViolation("Authorization header must be provided")

                val principal = tokenAuthService.authenticate(authHeader)

                val idParam = call.parameters["paymentId"]
                    ?: throw ContractViolation("payment id must be provided")

                val paymentId = idParam.toLongOrNull()
                    ?: throw ContractViolation("payment id must be a number")

                val result: PaymentDetailsResponse = paymentService.getPaymentDetails(
                    ownerId = principal.userId,
                    paymentId = paymentId,
                )

                call.respond(HttpStatusCode.OK, result)
            }

            get {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                    ?: throw ContractViolation("Authorization header must be provided")

                val principal = tokenAuthService.authenticate(authHeader)

                val status = call.request.queryParameters["status"]
                val fromAccountId = call.request.queryParameters["fromAccountId"]?.toLongOrNull()
                    ?: call.request.queryParameters["fromAccountId"]?.let { throw ContractViolation("fromAccountId must be a number") }

                val toAccountId = call.request.queryParameters["toAccountId"]?.toLongOrNull()
                    ?: call.request.queryParameters["toAccountId"]?.let { throw ContractViolation("toAccountId must be a number") }

                val currency = call.request.queryParameters["currency"]
                val createdFrom = call.request.queryParameters["createdFrom"]
                val createdTo = call.request.queryParameters["createdTo"]

                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                    ?: call.request.queryParameters["limit"]?.let { throw ContractViolation("limit must be a number") }
                    ?: 50

                val offset = call.request.queryParameters["offset"]?.toLongOrNull()
                    ?: call.request.queryParameters["offset"]?.let { throw ContractViolation("offset must be a number") }
                    ?: 0L

                val query = PaymentsSearchRequest(
                    status = status,
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    currency = currency,
                    createdFrom = createdFrom,
                    createdTo = createdTo,
                    limit = limit,
                    offset = offset,
                )

                val result = paymentService.searchPayments(
                    ownerId = principal.userId,
                    query = query,
                )

                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
