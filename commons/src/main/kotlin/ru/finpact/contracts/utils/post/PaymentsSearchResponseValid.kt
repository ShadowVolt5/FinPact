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

class PaymentsSearchResponseValid : Postcondition {

    private val moneyRegex = Regex("^\\d+(?:\\.\\d{1,4})?$")
    private val allowedStatuses = setOf("PENDING", "COMPLETED", "FAILED")

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation("result must not be null")
        val k = res::class

        val limit = readInt(k, res, "limit", "result.limit")
        val offset = readLong(k, res, "offset", "result.offset")
        val hasMore = readBoolean(k, res, "hasMore", "result.hasMore")
        val itemsAny = readAny(k, res, "items", "result.items")

        if (limit !in 1..200) throw ContractViolation("result.limit must be in 1..200")
        if (offset < 0L) throw ContractViolation("result.offset must be >= 0")

        val items = itemsAny as? Iterable<*> ?: throw ContractViolation("result.items must be Iterable")
        val list = items.toList()
        if (list.size > limit) throw ContractViolation("result.items size must be <= limit")
        if (hasMore && list.size != limit) throw ContractViolation("if hasMore=true, items size must equal limit")

        for ((idx, it) in list.withIndex()) {
            val item = it ?: throw ContractViolation("result.items[$idx] must not be null")
            val ik = item::class

            val id = readLong(ik, item, "id", "result.items[$idx].id")
            val status = readString(ik, item, "status", "result.items[$idx].status").trim()
            val fromId = readLong(ik, item, "fromAccountId", "result.items[$idx].fromAccountId")
            val toId = readLong(ik, item, "toAccountId", "result.items[$idx].toAccountId")
            val amount = parseMoney(readString(ik, item, "amount", "result.items[$idx].amount"), "result.items[$idx].amount")
            val currency = readString(ik, item, "currency", "result.items[$idx].currency").trim()
            val createdAt = readString(ik, item, "createdAt", "result.items[$idx].createdAt").trim()

            if (id <= 0L) throw ContractViolation("result.items[$idx].id must be positive")
            if (status !in allowedStatuses) throw ContractViolation("result.items[$idx].status is invalid")
            if (fromId <= 0L) throw ContractViolation("result.items[$idx].fromAccountId must be positive")
            if (toId <= 0L) throw ContractViolation("result.items[$idx].toAccountId must be positive")
            if (fromId == toId) throw ContractViolation("result.items[$idx] fromAccountId must differ from toAccountId")
            if (amount <= BigDecimal.ZERO) throw ContractViolation("result.items[$idx].amount must be positive")

            if (!(currency.length == 3 && currency.all { it.isLetter() } && currency == currency.uppercase())) {
                throw ContractViolation("result.items[$idx].currency must be 3-letter uppercase code")
            }
            if (!Currency.isSupported(currency)) {
                val allowed = Currency.supportedCodes().joinToString(",")
                throw ContractViolation("result.items[$idx].currency '$currency' is not supported (allowed: $allowed)")
            }

            try { Instant.parse(createdAt) } catch (_: Throwable) {
                throw ContractViolation("result.items[$idx].createdAt must be ISO-8601 instant")
            }
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

    private fun readAny(k: KClass<*>, obj: Any, name: String, path: String): Any {
        val p = k.memberProperties.firstOrNull { it.name == name } ?: throw ContractViolation("$path must be provided")
        return p.getter.call(obj) ?: throw ContractViolation("$path must be provided")
    }

    private fun readString(k: KClass<*>, obj: Any, name: String, path: String): String {
        val v = readAny(k, obj, name, path)
        return v as? String ?: throw ContractViolation("$path must be String")
    }

    private fun readLong(k: KClass<*>, obj: Any, name: String, path: String): Long {
        val v = readAny(k, obj, name, path)
        return v as? Long ?: throw ContractViolation("$path must be Long")
    }

    private fun readInt(k: KClass<*>, obj: Any, name: String, path: String): Int {
        val v = readAny(k, obj, name, path)
        return v as? Int ?: throw ContractViolation("$path must be Int")
    }

    private fun readBoolean(k: KClass<*>, obj: Any, name: String, path: String): Boolean {
        val v = readAny(k, obj, name, path)
        return v as? Boolean ?: throw ContractViolation("$path must be Boolean")
    }
}
