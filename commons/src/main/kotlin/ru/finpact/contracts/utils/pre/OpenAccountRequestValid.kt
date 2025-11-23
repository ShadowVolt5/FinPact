package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Precondition
import ru.finpact.model.Currency
import kotlin.reflect.full.memberProperties

class OpenAccountRequestValid : Precondition {

    override fun verify(ctx: ContractContext) {
        val dto = ctx.args.getOrNull(1)
            ?: throw ContractViolation("request must be provided")

        val kClass = dto::class

        val currencyRaw = kClass.memberProperties
            .firstOrNull { it.name == "currency" }
            ?.getter
            ?.call(dto) as? String
            ?: throw ContractViolation("currency must be provided")

        val currencyTrimmed = currencyRaw.trim()
        if (currencyTrimmed.isEmpty()) {
            throw ContractViolation("currency must not be blank")
        }
        if (currencyTrimmed.length != 3 || currencyTrimmed.any { !it.isLetter() }) {
            throw ContractViolation("currency must be 3-letter code")
        }

        val upper = currencyTrimmed.uppercase()
        if (!Currency.isSupported(upper)) {
            val allowed = Currency.supportedCodes().joinToString(",")
            throw ContractViolation("currency '$upper' is not supported (allowed: $allowed)")
        }

        val alias = kClass.memberProperties
            .firstOrNull { it.name == "alias" }
            ?.getter
            ?.call(dto) as? String?

        alias?.let {
            if (it.length > 100) {
                throw ContractViolation("alias is too long (max 100)")
            }
        }
    }
}
