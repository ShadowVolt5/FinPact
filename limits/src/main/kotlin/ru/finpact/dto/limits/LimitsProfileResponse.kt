package ru.finpact.dto.limits

import kotlinx.serialization.Serializable

@Serializable
data class LimitsProfileResponse(
    val ownerId: Long,
    val perTxn: String,
    val daily: String,
    val monthly: String,
    val currencies: List<String>,
)
