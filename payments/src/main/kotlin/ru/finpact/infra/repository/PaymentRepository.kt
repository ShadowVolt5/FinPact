package ru.finpact.infra.repository

import ru.finpact.model.PaymentDetails
import ru.finpact.model.Transfer
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
}
