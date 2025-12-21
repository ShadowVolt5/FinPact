package ru.finpact.contracts.utils.pre

import ru.finpact.contracts.core.*
import ru.finpact.model.Currency
import java.time.Instant
import kotlin.reflect.full.memberProperties

class PaymentsSearchQueryValid : Precondition {

    private val allowedStatuses = setOf("PENDING", "COMPLETED", "FAILED")

    override fun verify(ctx: ContractContext) {
        val q = ctx.arg<Any>("query")

        val status = q.readStringOrNull("status")?.trim()?.takeIf { it.isNotEmpty() }
        val fromId = q.readLongOrNull("fromAccountId")
        val toId = q.readLongOrNull("toAccountId")
        val currency = q.readStringOrNull("currency")?.trim()?.takeIf { it.isNotEmpty() }
        val createdFrom = q.readStringOrNull("createdFrom")?.trim()?.takeIf { it.isNotEmpty() }
        val createdTo = q.readStringOrNull("createdTo")?.trim()?.takeIf { it.isNotEmpty() }
        val limit = q.readIntOrNull("limit") ?: 50
        val offset = q.readLongOrNull("offset") ?: 0L

        if (limit !in 1..200) throw ContractViolation.badRequest("limit must be in 1..200")
        if (offset < 0L) throw ContractViolation.badRequest("offset must be >= 0")

        if (status != null && status !in allowedStatuses) {
            throw ContractViolation.badRequest("status is invalid")
        }

        if (fromId != null && fromId <= 0L) throw ContractViolation.badRequest("fromAccountId must be positive")
        if (toId != null && toId <= 0L) throw ContractViolation.badRequest("toAccountId must be positive")

        if (currency != null) {
            if (!(currency.length == 3 && currency.all { it.isLetter() } && currency == currency.uppercase())) {
                throw ContractViolation.badRequest("currency must be 3-letter uppercase code")
            }
            if (!Currency.isSupported(currency)) {
                val allowed = Currency.supportedCodes().joinToString(",")
                throw ContractViolation.badRequest("currency '$currency' is not supported (allowed: $allowed)")
            }
        }

        val fromInstant = createdFrom?.let {
            try { Instant.parse(it) } catch (_: Throwable) { throw ContractViolation.badRequest("createdFrom must be ISO-8601 instant") }
        }
        val toInstant = createdTo?.let {
            try { Instant.parse(it) } catch (_: Throwable) { throw ContractViolation.badRequest("createdTo must be ISO-8601 instant") }
        }

        if (fromInstant != null && toInstant != null && fromInstant.isAfter(toInstant)) {
            throw ContractViolation.badRequest("createdFrom must be <= createdTo")
        }
    }

    private fun Any.readStringOrNull(name: String): String? =
        this::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(this) as? String?

    private fun Any.readLongOrNull(name: String): Long? =
        this::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(this) as? Long?

    private fun Any.readIntOrNull(name: String): Int? =
        this::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(this) as? Int?
}
