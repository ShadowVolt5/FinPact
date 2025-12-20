package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

class TransferResponseMatchesRequest : Postcondition {

    override fun verify(ctx: ContractContext) {
        val req = ctx.args.getOrNull(1)
            ?: throw ContractViolation("request must be provided")

        val res = ctx.result
            ?: throw ContractViolation("result must not be null")

        val reqK = req::class
        val resK = res::class

        val reqFrom = (reqK.memberProperties.firstOrNull { it.name == "fromAccountId" }?.getter?.call(req) as? Long)
            ?: throw ContractViolation("request.fromAccountId must be provided")
        val reqTo = (reqK.memberProperties.firstOrNull { it.name == "toAccountId" }?.getter?.call(req) as? Long)
            ?: throw ContractViolation("request.toAccountId must be provided")

        val resFrom = (resK.memberProperties.firstOrNull { it.name == "fromAccountId" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation("result.fromAccountId must be provided")
        val resTo = (resK.memberProperties.firstOrNull { it.name == "toAccountId" }?.getter?.call(res) as? Long)
            ?: throw ContractViolation("result.toAccountId must be provided")

        if (reqFrom != resFrom) throw ContractViolation("result.fromAccountId must match request")
        if (reqTo != resTo) throw ContractViolation("result.toAccountId must match request")

        val reqAmountRaw = (reqK.memberProperties.firstOrNull { it.name == "amount" }?.getter?.call(req) as? String)
            ?: throw ContractViolation("request.amount must be provided")

        val resAmountRaw = (resK.memberProperties.firstOrNull { it.name == "amount" }?.getter?.call(res) as? String)
            ?: throw ContractViolation("result.amount must be provided")

        val reqAmount = try { BigDecimal(reqAmountRaw.trim()) } catch (_: Throwable) { throw ContractViolation("request.amount is invalid") }
        val resAmount = try { BigDecimal(resAmountRaw.trim()) } catch (_: Throwable) { throw ContractViolation("result.amount is invalid") }

        if (reqAmount.compareTo(resAmount) != 0) {
            throw ContractViolation("result.amount must match request")
        }
    }
}
