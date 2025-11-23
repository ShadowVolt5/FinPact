package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

/**
 * Проверяет:
 *  - что первый аргумент метода не null
 *  - что это строка вида "Bearer <token>"
 *  - что сам <token> не пустой
 */
class AuthHeaderPresent : Precondition {
    override fun verify(ctx: ContractContext) {
        val raw = ctx.args.getOrNull(0) as? String
            ?: throw ContractViolation("Authorization header must be provided")

        if (raw.isBlank()) {
            throw ContractViolation("Authorization header must not be blank")
        }

        if (!raw.startsWith("Bearer ")) {
            throw ContractViolation("Authorization header must start with 'Bearer '")
        }

        val token = raw.removePrefix("Bearer").trim()
        if (token.isEmpty()) {
            throw ContractViolation("Bearer token must not be empty")
        }
    }
}
