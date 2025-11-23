package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import kotlin.reflect.full.memberProperties

class EmailFormat : Precondition {
    private val regex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

    override fun verify(ctx: ContractContext) {
        val obj = ctx.args.getOrNull(0) ?: throw ContractViolation("request must not be null")
        val email = obj::class.memberProperties
            .firstOrNull { it.name == "email" }
            ?.getter?.call(obj) as? String
            ?: throw ContractViolation("email must be provided")

        if (email.isBlank() || !regex.matches(email.trim())) {
            throw ContractViolation("email format is invalid")
        }
    }
}
