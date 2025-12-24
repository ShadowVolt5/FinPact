package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class LimitsUsageResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = res::class

        val ownerId = readLong(k, res, "ownerId")
        if (ownerId <= 0L) throw ContractViolation.internal("result.ownerId must be positive")

        val day = readString(k, res, "day").trim()
        val monthStart = readString(k, res, "monthStart").trim()
        try { LocalDate.parse(day) } catch (_: Throwable) {
            throw ContractViolation.internal("result.day must be ISO date")
        }
        try { LocalDate.parse(monthStart) } catch (_: Throwable) {
            throw ContractViolation.internal("result.monthStart must be ISO date")
        }

        val dailyLimit = parseMoney(readString(k, res, "dailyLimit"), "result.dailyLimit")
        val dailyUsed = parseMoney(readString(k, res, "dailyUsed"), "result.dailyUsed")
        val dailyRemaining = parseMoney(readString(k, res, "dailyRemaining"), "result.dailyRemaining")

        val monthlyLimit = parseMoney(readString(k, res, "monthlyLimit"), "result.monthlyLimit")
        val monthlyUsed = parseMoney(readString(k, res, "monthlyUsed"), "result.monthlyUsed")
        val monthlyRemaining = parseMoney(readString(k, res, "monthlyRemaining"), "result.monthlyRemaining")

        if (dailyLimit < BigDecimal.ZERO) throw ContractViolation.internal("result.dailyLimit must be >= 0")
        if (monthlyLimit < BigDecimal.ZERO) throw ContractViolation.internal("result.monthlyLimit must be >= 0")

        if (dailyUsed < BigDecimal.ZERO) throw ContractViolation.internal("result.dailyUsed must be >= 0")
        if (monthlyUsed < BigDecimal.ZERO) throw ContractViolation.internal("result.monthlyUsed must be >= 0")

        if (dailyUsed > dailyLimit) throw ContractViolation.internal("result.dailyUsed must be <= result.dailyLimit")
        if (monthlyUsed > monthlyLimit) throw ContractViolation.internal("result.monthlyUsed must be <= result.monthlyLimit")

        val expectedDailyRemaining = maxZero(dailyLimit.subtract(dailyUsed))
        val expectedMonthlyRemaining = maxZero(monthlyLimit.subtract(monthlyUsed))

        if (dailyRemaining.compareTo(expectedDailyRemaining) != 0) {
            throw ContractViolation.internal("result.dailyRemaining must equal max(dailyLimit - dailyUsed, 0)")
        }
        if (monthlyRemaining.compareTo(expectedMonthlyRemaining) != 0) {
            throw ContractViolation.internal("result.monthlyRemaining must equal max(monthlyLimit - monthlyUsed, 0)")
        }
    }

    private fun maxZero(v: BigDecimal): BigDecimal =
        if (v.signum() < 0) BigDecimal.ZERO else v

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

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation.internal("result.$name must be provided")
}
