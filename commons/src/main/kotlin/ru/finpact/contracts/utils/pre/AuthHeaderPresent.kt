package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition

class AuthHeaderPresent : Precondition {
    override fun verify(ctx: ContractContext) {
        val raw = ctx.args.getOrNull(0) as? String
            ?: throw ContractViolation.unauthorized("Authorization header must be provided")

        if (raw.isBlank()) {
            throw ContractViolation.unauthorized("Authorization header must not be blank")
        }

        if (!raw.startsWith("Bearer ")) {
            throw ContractViolation.unauthorized("Authorization header must start with 'Bearer '")
        }

        val token = raw.removePrefix("Bearer").trim()
        if (token.isEmpty()) {
            throw ContractViolation.unauthorized("Bearer token must not be empty")
        }
    }
}
