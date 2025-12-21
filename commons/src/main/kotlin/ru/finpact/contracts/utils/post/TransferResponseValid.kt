package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import ru.finpact.model.Currency
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.max
import kotlin.reflect.full.memberProperties

class TransferResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = res::class

        val id = (k.memberProperties.firstOrNull { it.name == "id" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation.internal("result.id must be provided")
        if (id <= 0L) throw ContractViolation.internal("result.id must be positive")

        val fromId = (k.memberProperties.firstOrNull { it.name == "fromAccountId" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation.internal("result.fromAccountId must be provided")
        val toId = (k.memberProperties.firstOrNull { it.name == "toAccountId" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation.internal("result.toAccountId must be provided")

        if (fromId <= 0L) throw ContractViolation.internal("result.fromAccountId must be positive")
        if (toId <= 0L) throw ContractViolation.internal("result.toAccountId must be positive")
        if (fromId == toId) throw ContractViolation.internal("result accounts must be different")

        val amountRaw = (k.memberProperties.firstOrNull { it.name == "amount" }?.getter?.call(res) as? String)
            ?: throw ContractViolation.internal("result.amount must be provided")

        val amountTrimmed = amountRaw.trim()
        if (amountTrimmed.isEmpty()) throw ContractViolation.internal("result.amount must not be blank")
        if (!moneyRegex.matches(amountTrimmed)) throw ContractViolation.internal("result.amount must be plain decimal with scale <= 4")

        val amount = try { BigDecimal(amountTrimmed) } catch (_: Throwable) {
            throw ContractViolation.internal("result.amount must be a valid decimal number")
        }
        if (amount <= BigDecimal.ZERO) throw ContractViolation.internal("result.amount must be positive")
        if (amount.scale() > 4) throw ContractViolation.internal("result.amount scale must be <= 4")

        val integerDigits = max(0, amount.precision() - amount.scale())
        if (integerDigits > 15) throw ContractViolation.internal("result.amount integer digits must be <= 15")

        val currencyRaw = (k.memberProperties.firstOrNull { it.name == "currency" }?.getter?.call(res) as? String)
            ?: throw ContractViolation.internal("result.currency must be provided")

        val currency = currencyRaw.trim()
        if (currency.length != 3 || currency.any { !it.isLetter() } || currency != currency.uppercase()) {
            throw ContractViolation.internal("result.currency must be 3-letter uppercase code")
        }
        if (!Currency.isSupported(currency)) {
            val allowed = Currency.supportedCodes().joinToString(",")
            throw ContractViolation.internal("result.currency '$currency' is not supported (allowed: $allowed)")
        }

        val createdAtRaw = (k.memberProperties.firstOrNull { it.name == "createdAt" }?.getter?.call(res) as? String)
            ?: throw ContractViolation.internal("result.createdAt must be provided")

        val createdAt = createdAtRaw.trim()
        if (createdAt.isEmpty()) throw ContractViolation.internal("result.createdAt must not be blank")
        try { Instant.parse(createdAt) } catch (_: Throwable) {
            throw ContractViolation.internal("result.createdAt must be ISO-8601 instant")
        }
    }
}
