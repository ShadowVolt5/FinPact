package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.OpenAccountRequestValid
import ru.finpact.contracts.utils.OwnerIdPositive
import ru.finpact.contracts.utils.ResultNotNull
import ru.finpact.dto.create.OpenAccountRequest
import ru.finpact.dto.create.OpenAccountResponse

interface AccountService {

    @Pre(
        OwnerIdPositive::class,
        OpenAccountRequestValid::class,
    )
    @Post(ResultNotNull::class)
    fun openAccount(ownerId: Long, request: OpenAccountRequest): OpenAccountResponse
}
