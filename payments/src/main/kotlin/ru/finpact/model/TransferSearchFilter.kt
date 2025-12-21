package ru.finpact.model

import java.time.Instant

data class TransferSearchFilter(
    val status: PaymentStatus? = null,
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val currency: String? = null,
    val createdFrom: Instant? = null,
    val createdTo: Instant? = null,
)
