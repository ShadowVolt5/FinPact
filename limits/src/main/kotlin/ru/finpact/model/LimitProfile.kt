package ru.finpact.model

import java.math.BigDecimal

data class LimitProfile(
    val ownerId: Long,
    val perTxn: BigDecimal,
    val daily: BigDecimal,
    val monthly: BigDecimal,
    val currencies: List<String>,
)
