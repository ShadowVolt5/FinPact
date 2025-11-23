package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.*
import ru.finpact.dto.create.OpenAccountRequest
import ru.finpact.dto.common.AccountResponse

interface AccountService {

    @Pre(
        OwnerIdPositive::class,
        OpenAccountRequestValid::class,
    )
    @Post(ResultNotNull::class)
    fun openAccount(ownerId: Long, request: OpenAccountRequest): AccountResponse

    @Pre(
        AccountIdPositive::class,
        OwnerIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        AccountOwnedByCaller::class,
    )
    fun getAccount(accountId: Long, ownerId: Long): AccountResponse
}
