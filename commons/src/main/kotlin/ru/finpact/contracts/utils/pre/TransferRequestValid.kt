package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import java.math.BigDecimal
import kotlin.math.max
import kotlin.reflect.full.memberProperties

class TransferRequestValid : Precondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(1)
            ?: throw ContractViolation("request must be provided")

        val kClass = dto::class

        val fromId = (kClass.memberProperties.firstOrNull { it.name == "fromAccountId" }?.getter?.call(dto) as? Long)
            ?: throw ContractViolation("fromAccountId must be provided")

        val toId = (kClass.memberProperties.firstOrNull { it.name == "toAccountId" }?.getter?.call(dto) as? Long)
            ?: throw ContractViolation("toAccountId must be provided")

        if (fromId <= 0L) throw ContractViolation("fromAccountId must be positive")
        if (toId <= 0L) throw ContractViolation("toAccountId must be positive")
        if (fromId == toId) throw ContractViolation("fromAccountId and toAccountId must be different")

        val amountRaw = (kClass.memberProperties.firstOrNull { it.name == "amount" }?.getter?.call(dto) as? String)
            ?: throw ContractViolation("amount must be provided")

        val amountTrimmed = amountRaw.trim()
        if (amountTrimmed.isEmpty()) throw ContractViolation("amount must not be blank")
        if (!moneyRegex.matches(amountTrimmed)) throw ContractViolation("amount must be plain decimal with scale <= 4")

        val amount = try {
            BigDecimal(amountTrimmed)
        } catch (_: NumberFormatException) {
            throw ContractViolation("amount must be a valid decimal number")
        }

        if (amount <= BigDecimal.ZERO) throw ContractViolation("amount must be positive")

        if (amount.scale() > 4) throw ContractViolation("amount scale must be <= 4")
        val integerDigits = max(0, amount.precision() - amount.scale())
        if (integerDigits > 15) throw ContractViolation("amount integer digits must be <= 15")

        val description = kClass.memberProperties.firstOrNull { it.name == "description" }
            ?.getter?.call(dto) as? String?

        description?.let {
            if (it.length > 255) throw ContractViolation("description is too long (max 255)")
        }
    }
}
