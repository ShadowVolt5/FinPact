package ru.finpact.infra.repository

import ru.finpact.model.LimitProfile
import java.math.BigDecimal
import java.time.LocalDate

interface LimitsRepository {

    fun findProfile(ownerId: Long): LimitProfile?

    fun getDailyUsed(ownerId: Long, day: LocalDate): BigDecimal?

    fun getMonthlyUsed(ownerId: Long, monthStart: LocalDate): BigDecimal?
}
