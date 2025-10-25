package ru.finpact.dto.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val middleName: String? = null,
    val lastName: String
)
