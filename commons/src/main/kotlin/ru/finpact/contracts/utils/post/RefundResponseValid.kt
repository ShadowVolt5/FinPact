package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class RefundResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")
    private val allowedStatuses = setOf("PENDING", "COMPLETED", "FAILED")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation("result must not be null")
        val k = res::class

        val refundId = readLong(k, res, "refundPaymentId")
        if (refundId <= 0L) throw ContractViolation("result.refundPaymentId must be positive")

        val originalId = readLong(k, res, "originalPaymentId")
        if (originalId <= 0L) throw ContractViolation("result.originalPaymentId must be positive")

        val status = readString(k, res, "status").trim()
        if (status.isEmpty()) throw ContractViolation("result.status must not be blank")
        if (status !in allowedStatuses) throw ContractViolation("result.status is invalid")

        val amount = parseMoney(readString(k, res, "amount"), "result.amount")
        if (amount <= BigDecimal.ZERO) throw ContractViolation("result.amount must be positive")

        val currency = readString(k, res, "currency").trim()
        if (currency.length != 3 || currency.any { !it.isLetter() } || currency != currency.uppercase()) {
            throw ContractViolation("result.currency must be 3-letter uppercase code")
        }

        val createdAt = readString(k, res, "createdAt").trim()
        if (createdAt.isEmpty()) throw ContractViolation("result.createdAt must not be blank")
        try { Instant.parse(createdAt) } catch (_: Throwable) {
            throw ContractViolation("result.createdAt must be ISO-8601 instant")
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

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation("result.$name must be provided")
}
