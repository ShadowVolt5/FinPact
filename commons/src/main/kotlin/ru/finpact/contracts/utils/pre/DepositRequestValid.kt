package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

class DepositRequestValid : Precondition {

    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(2)
            ?: throw ContractViolation("request must be provided")

        val kClass = dto::class

        val amountRaw = kClass.memberProperties
            .firstOrNull { it.name == "amount" }
            ?.getter
            ?.call(dto) as? String
            ?: throw ContractViolation("amount must be provided")

        val amountTrimmed = amountRaw.trim()
        if (amountTrimmed.isEmpty()) {
            throw ContractViolation("amount must not be blank")
        }

        val amount = try {
            BigDecimal(amountTrimmed)
        } catch (_: NumberFormatException) {
            throw ContractViolation("amount must be a valid decimal number")
        }

        if (amount <= BigDecimal.ZERO) {
            throw ContractViolation("amount must be positive")
        }
    }
}
