package ru.finpact.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val id: Long,
    val ownerId: Long,
    val currency: String,
    val alias: String? = null,
    val balance: String,
    val isActive: Boolean,
    val createdAt: String,
)
