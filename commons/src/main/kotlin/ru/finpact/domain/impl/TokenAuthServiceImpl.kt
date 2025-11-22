package ru.finpact.domain.impl

import ru.finpact.auth.AuthPrincipal
import ru.finpact.auth.service.TokenAuthService
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.ports.SubjectExistencePort
import ru.finpact.jwt.JwtService

/**
 * Общая реализация контрактного сервиса аутентификации.
 * Ничего не знает о конкретной БД, работает через SubjectExistencePort.
 */
class TokenAuthServiceImpl(
    private val subjectPort: SubjectExistencePort
) : TokenAuthService {

    override fun authenticate(authorizationHeader: String): AuthPrincipal {
        val token = extractBearerToken(authorizationHeader)

        val userId = JwtService.extractUserIdFromToken(token)
        val email = JwtService.extractEmailFromToken(token)

        if (!subjectPort.subjectExists(userId)) {
            throw ContractViolation("subject does not exist")
        }

        return AuthPrincipal(
            userId = userId,
            email = email,
        )
    }

    private fun extractBearerToken(header: String): String =
        header.removePrefix("Bearer").trim().also {
            if (it.isEmpty()) {
                throw ContractViolation("Bearer token must not be empty")
            }
        }
}
