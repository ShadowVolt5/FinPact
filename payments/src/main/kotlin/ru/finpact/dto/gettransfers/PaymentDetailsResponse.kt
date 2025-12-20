package ru.finpact.dto.gettransfers

import kotlinx.serialization.Serializable

@Serializable
data class OwnerAccountSliceView(
    val id: Long,
    val currency: String,
    val balance: String,
    val isActive: Boolean,
)

@Serializable
data class CounterpartyAccountView(
    val id: Long,
    val currency: String,
)

@Serializable
data class PaymentDetailsResponse(
    val id: Long,
    val status: String,
    val from: OwnerAccountSliceView,
    val to: CounterpartyAccountView,
    val amount: String,
    val currency: String,
    val description: String? = null,
    val createdAt: String,
)


