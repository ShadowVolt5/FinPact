package ru.finpact.infra.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.finpact.contracts.core.ContractViolation

fun Application.installErrorHandling() {
    install(StatusPages) {
        exception<ContractViolation> { call, ex ->
            call.application.environment.log.warn("Contract violation: ${ex.message}", ex)
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = HttpStatusCode.BadRequest.value,
                    message = ex.message ?: DefaultErrorMessages.CONTRACT_VIOLATED,
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error on ${call.request.path()}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = HttpStatusCode.InternalServerError.value,
                    message = DefaultErrorMessages.INTERNAL_ERROR
                )
            )
        }
    }
}
