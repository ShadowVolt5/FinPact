package ru.finpact.dto.searchpayments

import kotlinx.serialization.Serializable

@Serializable
data class PaymentListItemResponse(
    val id: Long,
    val status: String,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: String,
    val currency: String,
    val description: String? = null,
    val createdAt: String,
)

@Serializable
data class PaymentsSearchResponse(
    val items: List<PaymentListItemResponse>,
    val limit: Int,
    val offset: Long,
    val hasMore: Boolean,
)
