package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.post.*
import ru.finpact.contracts.utils.pre.OwnerIdPositive
import ru.finpact.contracts.utils.pre.PaymentIdPositive
import ru.finpact.contracts.utils.pre.PaymentsSearchQueryValid
import ru.finpact.contracts.utils.pre.TransferRequestValid
import ru.finpact.dto.gettransfers.PaymentDetailsResponse
import ru.finpact.dto.searchpayments.PaymentsSearchRequest
import ru.finpact.dto.searchpayments.PaymentsSearchResponse
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

    @Pre(
        OwnerIdPositive::class,
        PaymentIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        PaymentDetailsMatchesRequest::class,
        PaymentDetailsResponseValid::class,
    )
    fun getPaymentDetails(ownerId: Long, paymentId: Long): PaymentDetailsResponse

    @Pre(
        OwnerIdPositive::class,
        PaymentsSearchQueryValid::class
    )
    @Post(
        ResultNotNull::class,
        PaymentsSearchResponseValid::class
    )
    fun searchPayments(ownerId: Long, query: PaymentsSearchRequest): PaymentsSearchResponse
}
