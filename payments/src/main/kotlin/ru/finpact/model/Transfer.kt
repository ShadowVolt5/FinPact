package ru.finpact.model

import java.math.BigDecimal
import java.time.Instant

data class Transfer(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val kind: PaymentKind,
    val refundOf: Long? = null,
    val description: String?,
    val initiatedBy: Long,
    val createdAt: Instant,
)
