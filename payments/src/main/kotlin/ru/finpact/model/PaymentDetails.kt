package ru.finpact.model

import java.math.BigDecimal
import java.time.Instant

data class OwnerAccountSlice(
    val id: Long,
    val currency: String,
    val balance: BigDecimal,
    val isActive: Boolean,
)

data class CounterpartyAccountRef(
    val id: Long,
    val currency: String,
)

data class PaymentDetails(
    val id: Long,
    val status: PaymentStatus,
    val from: OwnerAccountSlice,
    val to: CounterpartyAccountRef,
    val amount: BigDecimal,
    val currency: String,
    val description: String?,
    val createdAt: Instant,
)
