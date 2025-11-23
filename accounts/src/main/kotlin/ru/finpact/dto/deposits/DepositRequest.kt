package ru.finpact.dto.deposits

import kotlinx.serialization.Serializable

@Serializable
data class DepositRequest(
    val amount: String,
    val description: String? = null, // на текущий момент не используется в коде, заведено для будущего журнала истории
)
