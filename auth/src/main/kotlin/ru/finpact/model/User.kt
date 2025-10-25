package ru.finpact.model

data class User(
    val id: Long,
    val email: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val password: String,
)
