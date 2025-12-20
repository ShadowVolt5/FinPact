package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.post.ResultNotNull
import ru.finpact.contracts.utils.post.TransferResponseMatchesRequest
import ru.finpact.contracts.utils.post.TransferResponseValid
import ru.finpact.contracts.utils.pre.OwnerIdPositive
import ru.finpact.contracts.utils.pre.TransferRequestValid
import ru.finpact.dto.transfers.CreateTransferRequest
import ru.finpact.dto.transfers.TransferResponse

interface PaymentService {

    @Pre(
        OwnerIdPositive::class,
        TransferRequestValid::class,
    )
    @Post(
        ResultNotNull::class,
        TransferResponseMatchesRequest::class,
        TransferResponseValid::class,
    )
    fun createTransfer(ownerId: Long, request: CreateTransferRequest): TransferResponse
}
