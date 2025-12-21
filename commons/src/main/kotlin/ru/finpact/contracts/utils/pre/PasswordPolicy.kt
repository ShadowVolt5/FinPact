package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import kotlin.reflect.full.memberProperties

class PasswordPolicy : Precondition {
    override fun verify(ctx: ContractContext) {
        val obj = ctx.args.getOrNull(0) ?: throw ContractViolation.badRequest("request must not be null")
        val pwd = obj::class.memberProperties
            .firstOrNull { it.name == "password" }
            ?.getter?.call(obj) as? String
            ?: throw ContractViolation.badRequest("password must be provided")

        if (pwd.isBlank() || pwd.length < 8) {
            throw ContractViolation.badRequest("password does not meet policy (min length 8)")
        }
    }
}
