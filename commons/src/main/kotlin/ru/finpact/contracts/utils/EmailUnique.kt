package ru.finpact.contracts.utils

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import ru.finpact.contracts.ports.EmailUniquenessPort
import kotlin.reflect.full.memberProperties

class EmailUnique : Precondition {
    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(0) ?: throw ContractViolation("request must not be null")

        val email = dto::class.memberProperties
            .firstOrNull { it.name == "email" }
            ?.getter?.call(dto) as? String
            ?: throw ContractViolation("email must be provided")

        val port = ctx.target as? EmailUniquenessPort
            ?: throw IllegalStateException("Target does not implement EmailUniquenessPort")

        if (!port.isEmailFree(email.trim())) {
            throw ContractViolation("email is already taken")
        }
    }
}
