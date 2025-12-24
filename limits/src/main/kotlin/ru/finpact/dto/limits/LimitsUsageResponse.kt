package ru.finpact.dto.limits

import kotlinx.serialization.Serializable

@Serializable
data class LimitsUsageResponse(
    val ownerId: Long,
    val day: String,
    val dailyLimit: String,
    val dailyUsed: String,
    val dailyRemaining: String,
    val monthStart: String,
    val monthlyLimit: String,
    val monthlyUsed: String,
    val monthlyRemaining: String,
)
