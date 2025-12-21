package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import java.math.BigDecimal
import kotlin.math.max
import kotlin.reflect.full.memberProperties

class DepositRequestValid : Precondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val dto = ctx.arg<Any>("request")
        val k = dto::class

        val amountRaw = k.memberProperties
            .firstOrNull { it.name == "amount" }
            ?.getter?.call(dto) as? String
            ?: throw ContractViolation.badRequest("amount must be provided")

        val s = amountRaw.trim()
        if (s.isEmpty()) throw ContractViolation.badRequest("amount must not be blank")
        if (!moneyRegex.matches(s)) throw ContractViolation.badRequest("amount must be plain decimal with scale <= 4")

        val v = try { BigDecimal(s) } catch (_: Throwable) {
            throw ContractViolation.badRequest("amount must be a valid decimal number")
        }

        if (v <= BigDecimal.ZERO) throw ContractViolation.badRequest("amount must be positive")
        if (v.scale() > 4) throw ContractViolation.badRequest("amount scale must be <= 4")

        val integerDigits = max(0, v.precision() - v.scale())
        if (integerDigits > 15) throw ContractViolation.badRequest("amount integer digits must be <= 15")

        val description = k.memberProperties
            .firstOrNull { it.name == "description" }
            ?.getter?.call(dto) as? String?

        if (description != null && description.length > 255) {
            throw ContractViolation.badRequest("description is too long (max 255)")
        }
    }
}
