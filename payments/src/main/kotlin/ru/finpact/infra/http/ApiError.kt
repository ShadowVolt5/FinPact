package ru.finpact.infra.http

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: Int,
    val message: String,
)
