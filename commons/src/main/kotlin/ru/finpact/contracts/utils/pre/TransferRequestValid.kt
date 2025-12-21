package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import java.math.BigDecimal
import kotlin.math.max
import kotlin.reflect.full.memberProperties

class TransferRequestValid : Precondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val dto = ctx.arg<Any>("request")
        val k = dto::class

        val fromId = (k.memberProperties.firstOrNull { it.name == "fromAccountId" }?.getter?.call(dto) as? Long)
            ?: throw ContractViolation.badRequest("fromAccountId must be provided")

        val toId = (k.memberProperties.firstOrNull { it.name == "toAccountId" }?.getter?.call(dto) as? Long)
            ?: throw ContractViolation.badRequest("toAccountId must be provided")

        if (fromId <= 0L) throw ContractViolation.badRequest("fromAccountId must be positive")
        if (toId <= 0L) throw ContractViolation.badRequest("toAccountId must be positive")
        if (fromId == toId) throw ContractViolation.badRequest("fromAccountId and toAccountId must be different")

        val amountRaw = (k.memberProperties.firstOrNull { it.name == "amount" }?.getter?.call(dto) as? String)
            ?: throw ContractViolation.badRequest("amount must be provided")

        val s = amountRaw.trim()
        if (s.isEmpty()) throw ContractViolation.badRequest("amount must not be blank")
        if (!moneyRegex.matches(s)) throw ContractViolation.badRequest("amount must be plain decimal with scale <= 4")

        val amount = try { BigDecimal(s) } catch (_: Throwable) {
            throw ContractViolation.badRequest("amount must be a valid decimal number")
        }

        if (amount <= BigDecimal.ZERO) throw ContractViolation.badRequest("amount must be positive")
        if (amount.scale() > 4) throw ContractViolation.badRequest("amount scale must be <= 4")

        val integerDigits = max(0, amount.precision() - amount.scale())
        if (integerDigits > 15) throw ContractViolation.badRequest("amount integer digits must be <= 15")

        val description = k.memberProperties.firstOrNull { it.name == "description" }
            ?.getter?.call(dto) as? String?

        if (description != null && description.length > 255) {
            throw ContractViolation.badRequest("description is too long (max 255)")
        }
    }
}
