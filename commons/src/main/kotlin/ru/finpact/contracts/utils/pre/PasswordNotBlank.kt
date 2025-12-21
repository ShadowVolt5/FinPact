package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import kotlin.reflect.full.memberProperties

class PasswordNotBlank : Precondition {
    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(0) ?: throw ContractViolation.badRequest("request must not be null")

        val password = dto::class.memberProperties
            .firstOrNull { it.name == "password" }
            ?.getter?.call(dto) as? String
            ?: throw ContractViolation.badRequest("password must be provided")

        if (password.isBlank()) {
            throw ContractViolation.badRequest("password must not be blank")
        }
    }
}
