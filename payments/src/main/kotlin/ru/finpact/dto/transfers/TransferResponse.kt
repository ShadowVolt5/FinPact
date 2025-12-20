package ru.finpact.dto.transfers

import kotlinx.serialization.Serializable

@Serializable
data class TransferResponse(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: String,
    val currency: String,
    val description: String? = null,
    val createdAt: String,
)
