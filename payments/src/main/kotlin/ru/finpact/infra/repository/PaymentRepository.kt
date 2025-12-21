package ru.finpact.infra.repository

import ru.finpact.model.PaymentDetails
import ru.finpact.model.Transfer
import ru.finpact.model.TransferSearchFilter
import ru.finpact.model.TransferSearchPage
import java.math.BigDecimal

interface PaymentRepository {

    fun createTransfer(
        initiatedBy: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: BigDecimal,
        description: String?,
    ): Transfer

    fun findPaymentDetails(
        initiatedBy: Long,
        paymentId: Long,
    ): PaymentDetails?

    fun searchTransfers(
        initiatedBy: Long,
        filter: TransferSearchFilter,
        limit: Int,
        offset: Long,
    ): TransferSearchPage
}
