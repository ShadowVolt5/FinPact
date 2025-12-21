package ru.finpact.domain.impl

import ru.finpact.contracts.annotations.Invariants
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.utils.invariants.StatelessServiceInvariant
import ru.finpact.domain.PaymentService
import ru.finpact.dto.gettransfers.CounterpartyAccountView
import ru.finpact.dto.gettransfers.OwnerAccountSliceView
import ru.finpact.dto.gettransfers.PaymentDetailsResponse
import ru.finpact.dto.refunds.RefundResponse
import ru.finpact.dto.searchpayments.PaymentListItemResponse
import ru.finpact.dto.searchpayments.PaymentsSearchRequest
import ru.finpact.dto.searchpayments.PaymentsSearchResponse
import ru.finpact.dto.transfers.CreateTransferRequest
import ru.finpact.dto.transfers.TransferResponse
import ru.finpact.infra.repository.PaymentRepository
import ru.finpact.model.PaymentStatus
import ru.finpact.model.TransferSearchFilter
import java.math.BigDecimal
import java.time.Instant

@Invariants(StatelessServiceInvariant::class)
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository
) : PaymentService {

    override fun createTransfer(ownerId: Long, request: CreateTransferRequest): TransferResponse {
        val amount = BigDecimal(request.amount.trim())
        val description = request.description?.trim()?.ifBlank { null }

        val transfer = paymentRepository.createTransfer(
            initiatedBy = ownerId,
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            amount = amount,
            description = description,
        )

        return TransferResponse(
            id = transfer.id,
            fromAccountId = transfer.fromAccountId,
            toAccountId = transfer.toAccountId,
            amount = transfer.amount.stripTrailingZeros().toPlainString(),
            currency = transfer.currency,
            description = transfer.description,
            createdAt = transfer.createdAt.toString(),
        )
    }

    override fun getPaymentDetails(ownerId: Long, paymentId: Long): PaymentDetailsResponse {
        val details = paymentRepository.findPaymentDetails(
            initiatedBy = ownerId,
            paymentId = paymentId,
        ) ?: throw ContractViolation("payment not found")

        return PaymentDetailsResponse(
            id = details.id,
            status = details.status.name,
            from = OwnerAccountSliceView(
                id = details.from.id,
                currency = details.from.currency,
                balance = details.from.balance.stripTrailingZeros().toPlainString(),
                isActive = details.from.isActive,
            ),
            to = CounterpartyAccountView(
                id = details.to.id,
                currency = details.to.currency,
            ),
            amount = details.amount.stripTrailingZeros().toPlainString(),
            currency = details.currency,
            description = details.description,
            createdAt = details.createdAt.toString(),
        )
    }

    override fun searchPayments(ownerId: Long, query: PaymentsSearchRequest): PaymentsSearchResponse {
        val status = query.status?.trim()?.takeIf { it.isNotEmpty() }?.let { PaymentStatus.valueOf(it) }

        val filter = TransferSearchFilter(
            status = status,
            fromAccountId = query.fromAccountId,
            toAccountId = query.toAccountId,
            currency = query.currency?.trim()?.takeIf { it.isNotEmpty() },
            createdFrom = query.createdFrom?.trim()?.takeIf { it.isNotEmpty() }?.let(Instant::parse),
            createdTo = query.createdTo?.trim()?.takeIf { it.isNotEmpty() }?.let(Instant::parse),
        )

        val page = paymentRepository.searchTransfers(
            initiatedBy = ownerId,
            filter = filter,
            limit = query.limit,
            offset = query.offset,
        )

        return PaymentsSearchResponse(
            items = page.items.map { t ->
                PaymentListItemResponse(
                    id = t.id,
                    status = t.status.name,
                    fromAccountId = t.fromAccountId,
                    toAccountId = t.toAccountId,
                    amount = t.amount.stripTrailingZeros().toPlainString(),
                    currency = t.currency,
                    description = t.description,
                    createdAt = t.createdAt.toString(),
                )
            },
            limit = query.limit,
            offset = query.offset,
            hasMore = page.hasMore,
        )
    }

    override fun createRefund(ownerId: Long, paymentId: Long): RefundResponse {
        val refund = paymentRepository.createRefund(
            initiatedBy = ownerId,
            originalPaymentId = paymentId,
        )

        return RefundResponse(
            refundPaymentId = refund.id,
            originalPaymentId = paymentId,
            status = refund.status.name,
            amount = refund.amount.stripTrailingZeros().toPlainString(),
            currency = refund.currency,
            createdAt = refund.createdAt.toString(),
        )
    }
}
