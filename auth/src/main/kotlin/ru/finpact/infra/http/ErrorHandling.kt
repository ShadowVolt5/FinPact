package ru.finpact.infra.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.infra.http.dto.ApiError

fun Application.installErrorHandling() {
    install(StatusPages) {
        exception<ContractViolation> {call, ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = HttpStatusCode.BadRequest.value,
                    message = ex.message ?: DefaultErrorMessages.CONTRACT_VIOLATED,
                )
            )
        }
        exception<Throwable> { call, _ ->
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
