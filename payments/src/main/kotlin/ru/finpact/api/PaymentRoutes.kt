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
import ru.finpact.dto.refunds.RefundListResponse
import ru.finpact.dto.refunds.RefundResponse
import ru.finpact.dto.searchpayments.PaymentsSearchRequest
import ru.finpact.dto.searchpayments.PaymentsSearchResponse
import ru.finpact.dto.transfers.CreateTransferRequest
import ru.finpact.dto.transfers.TransferResponse
import ru.finpact.fx.impl.CbrFxRateProvider
import ru.finpact.infra.repository.impl.LimitsRepositoryImpl
import ru.finpact.infra.repository.impl.PaymentRepositoryImpl
import ru.finpact.infra.repository.impl.UsersRepositoryImpl

fun Application.paymentRoutes() {
    val usersRepo = UsersRepositoryImpl()
    val subjectPort = SubjectExistencePortImpl(usersRepo)

    val rawTokenAuthService = TokenAuthServiceImpl(subjectPort)
    val tokenAuthService: TokenAuthService =
        ContractProxy.wrap<TokenAuthService>(rawTokenAuthService)

    val limitsRepo = LimitsRepositoryImpl()
    val fxProvider = CbrFxRateProvider()

    val paymentsRepo = PaymentRepositoryImpl(
        limitsRepository = limitsRepo,
        fxRateProvider = fxProvider,
    )

    val rawPaymentService = PaymentServiceImpl(paymentsRepo)
    val paymentService: PaymentService =
        ContractProxy.wrap<PaymentService>(rawPaymentService)

    routing {
        route("/payments") {

            post("/transfers") {
                val principal = call.requirePrincipal(tokenAuthService)
                val request = call.receive<CreateTransferRequest>()

                val result: TransferResponse = paymentService.createTransfer(
                    ownerId = principal.userId,
                    request = request,
                )

                call.respond(HttpStatusCode.Created, result)
            }

            post("/{paymentId}/refunds") {
                val principal = call.requirePrincipal(tokenAuthService)
                val paymentId = call.requirePathLong("paymentId", "payment id")

                val result: RefundResponse = paymentService.createRefund(
                    ownerId = principal.userId,
                    paymentId = paymentId,
                )

                call.respond(HttpStatusCode.Created, result)
            }

            get("/{paymentId}") {
                val principal = call.requirePrincipal(tokenAuthService)
                val paymentId = call.requirePathLong("paymentId", "payment id")

                val result: PaymentDetailsResponse = paymentService.getPaymentDetails(
                    ownerId = principal.userId,
                    paymentId = paymentId,
                )

                call.respond(HttpStatusCode.OK, result)
            }

            get {
                val principal = call.requirePrincipal(tokenAuthService)

                val query = PaymentsSearchRequest(
                    status = call.queryStringOrNull("status"),
                    fromAccountId = call.queryLongOrNull("fromAccountId", "fromAccountId"),
                    toAccountId = call.queryLongOrNull("toAccountId", "toAccountId"),
                    currency = call.queryStringOrNull("currency"),
                    createdFrom = call.queryStringOrNull("createdFrom"),
                    createdTo = call.queryStringOrNull("createdTo"),
                    limit = call.queryIntOrDefault("limit", "limit", 50),
                    offset = call.queryLongOrDefault("offset", "offset", 0L),
                )

                val result: PaymentsSearchResponse = paymentService.searchPayments(
                    ownerId = principal.userId,
                    query = query,
                )

                call.respond(HttpStatusCode.OK, result)
            }

            get("/{paymentId}/refunds") {
                val principal = call.requirePrincipal(tokenAuthService)
                val paymentId = call.requirePathLong("paymentId", "payment id")

                val result: RefundListResponse = paymentService.listRefunds(
                    ownerId = principal.userId,
                    paymentId = paymentId,
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

private fun ApplicationCall.queryStringOrNull(name: String): String? =
    request.queryParameters[name]

private fun ApplicationCall.queryLongOrNull(name: String, humanName: String): Long? {
    val raw = request.queryParameters[name] ?: return null
    return raw.toLongOrNull()
        ?: throw ContractViolation.badRequest("$humanName must be a number")
}

private fun ApplicationCall.queryIntOrDefault(name: String, humanName: String, default: Int): Int {
    val raw = request.queryParameters[name] ?: return default
    return raw.toIntOrNull()
        ?: throw ContractViolation.badRequest("$humanName must be a number")
}

private fun ApplicationCall.queryLongOrDefault(name: String, humanName: String, default: Long): Long {
    val raw = request.queryParameters[name] ?: return default
    return raw.toLongOrNull()
        ?: throw ContractViolation.badRequest("$humanName must be a number")
}
