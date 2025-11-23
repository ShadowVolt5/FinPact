package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import kotlin.reflect.full.memberProperties

class PasswordNotBlank : Precondition {
    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(0) ?: throw ContractViolation("request must not be null")

        val password = dto::class.memberProperties
            .firstOrNull { it.name == "password" }
            ?.getter?.call(dto) as? String
            ?: throw ContractViolation("password must be provided")

        if (password.isBlank()) {
            throw ContractViolation("password must not be blank")
        }
    }
}

