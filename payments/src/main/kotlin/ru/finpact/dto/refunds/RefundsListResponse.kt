package ru.finpact.dto.refunds

import kotlinx.serialization.Serializable

@Serializable
data class RefundListResponse(
    val items: List<RefundResponse>,
)
