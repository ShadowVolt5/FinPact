package ru.finpact.dto.refunds

import kotlinx.serialization.Serializable

@Serializable
data class RefundResponse(
    val refundPaymentId: Long,
    val originalPaymentId: Long,
    val status: String,
    val amount: String,
    val currency: String,
    val createdAt: String,
)
