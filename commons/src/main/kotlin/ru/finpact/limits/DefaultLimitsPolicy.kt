package ru.finpact.limits

import ru.finpact.model.Currency
import java.math.BigDecimal

object DefaultLimitsPolicy {

    val BASE_CURRENCY = arrayOf("RUB", "USD", "EUR")

    val PER_TXN: BigDecimal = BigDecimal("100000.0000")
    val DAILY: BigDecimal = BigDecimal("300000.0000")
    val MONTHLY: BigDecimal = BigDecimal("2000000.0000")
}
