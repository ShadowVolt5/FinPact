package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import ru.finpact.model.Currency
import kotlin.reflect.full.memberProperties

class OpenAccountRequestValid : Precondition {

    override fun verify(ctx: ContractContext) {
        val dto = ctx.arg<Any>("request")
        val k = dto::class

        val currencyRaw = k.memberProperties
            .firstOrNull { it.name == "currency" }
            ?.getter?.call(dto) as? String
            ?: throw ContractViolation.badRequest("currency must be provided")

        val currencyTrimmed = currencyRaw.trim()
        if (currencyTrimmed.isEmpty()) throw ContractViolation.badRequest("currency must not be blank")
        if (currencyTrimmed.length != 3 || currencyTrimmed.any { !it.isLetter() }) {
            throw ContractViolation.badRequest("currency must be 3-letter code")
        }

        val upper = currencyTrimmed.uppercase()
        if (!Currency.isSupported(upper)) {
            val allowed = Currency.supportedCodes().joinToString(",")
            throw ContractViolation.badRequest("currency '$upper' is not supported (allowed: $allowed)")
        }

        val alias = k.memberProperties
            .firstOrNull { it.name == "alias" }
            ?.getter?.call(dto) as? String?

        if (alias != null && alias.length > 100) {
            throw ContractViolation.badRequest("alias is too long (max 100)")
        }
    }
}
