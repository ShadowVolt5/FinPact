package ru.finpact.auth

data class AuthPrincipal(
    val userId: Long,
    val email: String,
)
