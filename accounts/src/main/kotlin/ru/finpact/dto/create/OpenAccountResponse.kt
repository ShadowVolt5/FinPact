package ru.finpact.dto.create

import kotlinx.serialization.Serializable

@Serializable
data class OpenAccountResponse(
    val id: Long,
    val ownerId: Long,
    val currency: String,
    val alias: String? = null,
    val balance: String,
    val isActive: Boolean,
)
