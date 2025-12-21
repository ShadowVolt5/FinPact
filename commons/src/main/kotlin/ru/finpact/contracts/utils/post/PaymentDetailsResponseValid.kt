package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import ru.finpact.model.Currency
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class PaymentDetailsResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")
    private val allowedStatuses = setOf("PENDING", "COMPLETED", "FAILED")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = res::class

        val id = readLong(k, res, "id")
        if (id <= 0L) throw ContractViolation.internal("result.id must be positive")

        val status = readString(k, res, "status").trim()
        if (status.isEmpty()) throw ContractViolation.internal("result.status must not be blank")
        if (status !in allowedStatuses) throw ContractViolation.internal("result.status is invalid")

        val amount = parseMoney(readString(k, res, "amount"), "result.amount")
        if (amount <= BigDecimal.ZERO) throw ContractViolation.internal("result.amount must be positive")

        val currency = readString(k, res, "currency").trim()
        if (!isCurrencyUpper3(currency)) throw ContractViolation.internal("result.currency must be 3-letter uppercase code")
        if (!Currency.isSupported(currency)) {
            val allowed = Currency.supportedCodes().joinToString(",")
            throw ContractViolation.internal("result.currency '$currency' is not supported (allowed: $allowed)")
        }

        val createdAt = readString(k, res, "createdAt").trim()
        if (createdAt.isEmpty()) throw ContractViolation.internal("result.createdAt must not be blank")
        try { Instant.parse(createdAt) } catch (_: Throwable) {
            throw ContractViolation.internal("result.createdAt must be ISO-8601 instant")
        }

        val fromObj = readAny(k, res, "from")
        val fromK = fromObj::class
        val fromId = readLong(fromK, fromObj, "id")
        val fromCurrency = readString(fromK, fromObj, "currency").trim()
        val fromBalance = parseMoney(readString(fromK, fromObj, "balance"), "result.from.balance")
        readBoolean(fromK, fromObj, "isActive") // must be present

        if (fromId <= 0L) throw ContractViolation.internal("result.from.id must be positive")
        if (!isCurrencyUpper3(fromCurrency)) throw ContractViolation.internal("result.from.currency must be 3-letter uppercase code")
        if (fromCurrency != currency) throw ContractViolation.internal("result.from.currency must match result.currency")
        if (fromBalance < BigDecimal.ZERO) throw ContractViolation.internal("result.from.balance must be >= 0")

        val toObj = readAny(k, res, "to")
        val toK = toObj::class
        val toId = readLong(toK, toObj, "id")
        val toCurrency = readString(toK, toObj, "currency").trim()

        if (toId <= 0L) throw ContractViolation.internal("result.to.id must be positive")
        if (toId == fromId) throw ContractViolation.internal("result.from.id and result.to.id must be different")
        if (!isCurrencyUpper3(toCurrency)) throw ContractViolation.internal("result.to.currency must be 3-letter uppercase code")
        if (toCurrency != currency) throw ContractViolation.internal("result.to.currency must match result.currency")
    }

    private fun parseMoney(raw: String, field: String): BigDecimal {
        val s = raw.trim()
        if (s.isEmpty()) throw ContractViolation.internal("$field must not be blank")
        if (!moneyRegex.matches(s)) throw ContractViolation.internal("$field must be plain decimal with scale <= 4")

        val v = try { BigDecimal(s) } catch (_: Throwable) {
            throw ContractViolation.internal("$field must be a valid decimal number")
        }

        if (v.scale() > 4) throw ContractViolation.internal("$field scale must be <= 4")
        val integerDigits = max(0, v.precision() - v.scale())
        if (integerDigits > 15) throw ContractViolation.internal("$field integer digits must be <= 15")
        return v
    }

    private fun isCurrencyUpper3(s: String): Boolean =
        s.length == 3 && s.all { it.isLetter() } && s == s.uppercase()

    private fun readAny(k: KClass<*>, obj: Any, name: String): Any =
        k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readBoolean(k: KClass<*>, obj: Any, name: String): Boolean =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Boolean)
            ?: throw ContractViolation.internal("result.$name must be provided")
}
