package ru.finpact.infra.repository

import ru.finpact.model.Account
import java.math.BigDecimal

interface AccountsRepository {

    fun createAccount(
        ownerId: Long,
        currency: String,
        alias: String?,
        initialBalance: BigDecimal = BigDecimal.ZERO,
    ): Account
}
