package ru.finpact.dto.created

import kotlinx.serialization.Serializable

@Serializable
data class OpenAccountRequest(
    val currency: String,
    val alias: String? = null,
)
