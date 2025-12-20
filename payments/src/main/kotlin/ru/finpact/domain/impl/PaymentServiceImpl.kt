package ru.finpact.domain.impl

import ru.finpact.contracts.annotations.Invariants
import ru.finpact.contracts.utils.invariants.StatelessServiceInvariant
import ru.finpact.domain.PaymentService
import ru.finpact.dto.transfers.CreateTransferRequest
import ru.finpact.dto.transfers.TransferResponse
import ru.finpact.infra.repository.PaymentRepository
import java.math.BigDecimal

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
}
