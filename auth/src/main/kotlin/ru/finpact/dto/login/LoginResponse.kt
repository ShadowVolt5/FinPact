package ru.finpact.dto.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val user: UserView,
    val token: String
)

@Serializable
data class UserView(
    val id: Long,
    val email: String,
    val firstName: String,
    val middleName: String? = null,
    val lastName: String
)
