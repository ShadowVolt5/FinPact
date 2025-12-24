package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import ru.finpact.model.Currency
import java.math.BigDecimal
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class LimitsProfileResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = res::class

        val ownerId = readLong(k, res, "ownerId")
        if (ownerId <= 0L) throw ContractViolation.internal("result.ownerId must be positive")

        val perTxn = parseMoney(readString(k, res, "perTxn"), "result.perTxn")
        val daily = parseMoney(readString(k, res, "daily"), "result.daily")
        val monthly = parseMoney(readString(k, res, "monthly"), "result.monthly")

        if (perTxn < BigDecimal.ZERO) throw ContractViolation.internal("result.perTxn must be >= 0")
        if (daily < BigDecimal.ZERO) throw ContractViolation.internal("result.daily must be >= 0")
        if (monthly < BigDecimal.ZERO) throw ContractViolation.internal("result.monthly must be >= 0")

        if (daily < perTxn) throw ContractViolation.internal("result.daily must be >= result.perTxn")
        if (monthly < daily) throw ContractViolation.internal("result.monthly must be >= result.daily")

        val currenciesAny = readAny(k, res, "currencies", "result.currencies")
        val currencies = currenciesAny as? Iterable<*> ?: throw ContractViolation.internal("result.currencies must be Iterable")
        val list = currencies.map { it?.toString() ?: "" }.toList()

        if (list.isEmpty()) throw ContractViolation.internal("result.currencies must not be empty")

        for ((idx, c) in list.withIndex()) {
            val code = c.trim()
            if (!(code.length == 3 && code.all { it.isLetter() } && code == code.uppercase())) {
                throw ContractViolation.internal("result.currencies[$idx] must be 3-letter uppercase code")
            }
            if (!Currency.isSupported(code)) {
                val allowed = Currency.supportedCodes().joinToString(",")
                throw ContractViolation.internal("result.currencies[$idx] '$code' is not supported (allowed: $allowed)")
            }
        }
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

    private fun readAny(k: KClass<*>, obj: Any, name: String, path: String): Any {
        val p = k.memberProperties.firstOrNull { it.name == name }
            ?: throw ContractViolation.internal("$path must be provided")
        return p.getter.call(obj) ?: throw ContractViolation.internal("$path must be provided")
    }

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readLong(k: KClass<*>, obj: Any, name: String): Long =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Long)
            ?: throw ContractViolation.internal("result.$name must be provided")
}
