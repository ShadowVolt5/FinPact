package ru.finpact.dto.searchpayments

data class PaymentsSearchRequest(
    val status: String? = null,
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val currency: String? = null,
    val createdFrom: String? = null,
    val createdTo: String? = null,
    val limit: Int = 50,
    val offset: Long = 0L,
)
