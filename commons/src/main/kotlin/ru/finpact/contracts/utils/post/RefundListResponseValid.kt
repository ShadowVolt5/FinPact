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

class RefundListResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")
    private val allowedStatuses = setOf("PENDING", "COMPLETED", "FAILED")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation("result must not be null")

        val itemsAny = readAny(res::class, res, "items")
        if (itemsAny !is List<*>) throw ContractViolation("result.items must be a list")

        for ((idx, item) in itemsAny.withIndex()) {
            val it = item ?: throw ContractViolation("result.items[$idx] must not be null")
            validateRefund(it, idx)
        }
    }

    private fun validateRefund(obj: Any, idx: Int) {
        val k = obj::class

        val refundPaymentId = readLong(k, obj, "refundPaymentId")
        if (refundPaymentId <= 0L) throw ContractViolation("result.items[$idx].refundPaymentId must be positive")

        val originalPaymentId = readLong(k, obj, "originalPaymentId")
        if (originalPaymentId <= 0L) throw ContractViolation("result.items[$idx].originalPaymentId must be positive")

        val status = readString(k, obj, "status").trim()
        if (status.isEmpty()) throw ContractViolation("result.items[$idx].status must not be blank")
        if (status !in allowedStatuses) throw ContractViolation("result.items[$idx].status is invalid")

        val amount = parseMoney(readString(k, obj, "amount"), "result.items[$idx].amount")
        if (amount <= BigDecimal.ZERO) throw ContractViolation("result.items[$idx].amount must be positive")

        val currency = readString(k, obj, "currency").trim()
        if (!isCurrencyUpper3(currency)) throw ContractViolation("result.items[$idx].currency must be 3-letter uppercase code")
        if (!Currency.isSupported(currency)) {
            val allowed = Currency.supportedCodes().joinToString(",")
            throw ContractViolation("result.items[$idx].currency '$currency' is not supported (allowed: $allowed)")
        }

        val createdAt = readString(k, obj, "createdAt").trim()
        if (createdAt.isEmpty()) throw ContractViolation("result.items[$idx].createdAt must not be blank")
        try { Instant.parse(createdAt) } catch (_: Throwable) {
            throw ContractViolation("result.items[$idx].createdAt must be ISO-8601 instant")
        }
    }

    private fun parseMoney(raw: String, field: String): BigDecimal {
        val s = raw.trim()
        if (s.isEmpty()) throw ContractViolation("$field must not be blank")
        if (!moneyRegex.matches(s)) throw ContractViolation("$field must be plain decimal with scale <= 4")

        val v = try { BigDecimal(s) } catch (_: Throwable) {
            throw ContractViolation("$field must be a valid decimal number")
        }

        if (v.scale() > 4) throw ContractViolation("$field scale must be <= 4")
        val integerDigits = max(0, v.precision() - v.scale())
        if (integerDigits > 15) throw ContractViolation("$field integer digits must be <= 15")
        return v
    }

    private fun isCurrencyUpper3(s: String): Boolean =
        s.length == 3 && s.all { it.isLetter() } && s == s.uppercase()

    private fun readAny(k: KClass<*>, obj: Any, name: String): Any =
        k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj)
            ?: throw ContractViolation("result.$name must be provided")

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation("result.$name must be provided")
}
