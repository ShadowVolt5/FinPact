package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import kotlin.reflect.full.memberProperties

class PasswordPolicy : Precondition {
    override fun verify(ctx: ContractContext) {
        val obj = ctx.args.getOrNull(0) ?: throw ContractViolation("request must not be null")
        val pwd = obj::class.memberProperties
            .firstOrNull { it.name == "password" }
            ?.getter?.call(obj) as? String
            ?: throw ContractViolation("password must be provided")

        if (pwd.isBlank() || pwd.length < 8) {
            throw ContractViolation("password does not meet policy (min length 8)")
        }
    }
}
