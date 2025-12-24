package ru.finpact.fx

import java.math.BigDecimal
import java.math.RoundingMode

interface FxRateProvider {

    fun rubPerUnit(currency: String): BigDecimal

    fun toRub(amount: BigDecimal, currency: String): BigDecimal {
        val code = currency.trim().uppercase()
        val rate = rubPerUnit(code)
        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP)
    }
}
