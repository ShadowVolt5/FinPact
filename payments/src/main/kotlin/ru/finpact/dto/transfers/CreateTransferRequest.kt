package ru.finpact.dto.transfers

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransferRequest(
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: String,
    val description: String? = null,
)
