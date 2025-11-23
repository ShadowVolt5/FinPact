package ru.finpact.model

import java.math.BigDecimal
import java.time.Instant

data class Account(
    val id: Long,
    val ownerId: Long,
    val currency: String,
    val alias: String?,
    val balance: BigDecimal,
    val isActive: Boolean,
    val createdAt: Instant,
)
