package ru.finpact.contracts.ports

interface EmailUniquenessPort {
    fun isEmailFree(email: String): Boolean
}

