package ru.finpact.infra.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: Int,
    val message: String,
)
