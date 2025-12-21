package ru.finpact.infra.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.ContractViolationKind
import ru.finpact.infra.http.dto.ApiError

fun Application.installErrorHandling() {
    install(StatusPages) {

        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = HttpStatusCode.BadRequest.value,
                    message = cause.message?.takeIf { it.isNotBlank() } ?: DefaultErrorMessages.INVALID_REQUEST
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = HttpStatusCode.BadRequest.value,
                    message = cause.message?.takeIf { it.isNotBlank() } ?: DefaultErrorMessages.INVALID_REQUEST
                )
            )
        }

        exception<ContractViolation> { call, ex ->
            val status = ex.kind.toHttpStatusCode()

            val message = if (ex.kind == ContractViolationKind.INTERNAL) {
                DefaultErrorMessages.INTERNAL_ERROR
            } else {
                ex.message ?: DefaultErrorMessages.CONTRACT_VIOLATED
            }

            call.respond(
                status,
                ApiError(
                    code = status.value,
                    message = message
                )
            )
        }

        exception<Throwable> { call, t ->
            call.application.log.error("Unhandled error", t)
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

private fun ContractViolationKind.toHttpStatusCode(): HttpStatusCode = when (this) {
    ContractViolationKind.BAD_REQUEST -> HttpStatusCode.BadRequest
    ContractViolationKind.UNAUTHORIZED -> HttpStatusCode.Unauthorized
    ContractViolationKind.FORBIDDEN -> HttpStatusCode.Forbidden
    ContractViolationKind.NOT_FOUND -> HttpStatusCode.NotFound
    ContractViolationKind.CONFLICT -> HttpStatusCode.Conflict
    ContractViolationKind.INTERNAL -> HttpStatusCode.InternalServerError
}
