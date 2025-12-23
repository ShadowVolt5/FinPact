package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class AccountClosedWithZeroBalance : Postcondition {

    override fun verify(ctx: ContractContext) {
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")
        val k = res::class

        val isActive = readBoolean(k, res, "isActive")
        if (isActive) throw ContractViolation.internal("result.isActive must be false for closed account")

        val balanceRaw = readString(k, res, "balance").trim()
        val balance = try { BigDecimal(balanceRaw) } catch (_: Throwable) {
            throw ContractViolation.internal("result.balance must be a valid decimal number")
        }

        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw ContractViolation.internal("result.balance must be zero for closed account")
        }
    }

    private fun readString(k: KClass<*>, obj: Any, name: String): String =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? String)
            ?: throw ContractViolation.internal("result.$name must be provided")

    private fun readBoolean(k: KClass<*>, obj: Any, name: String): Boolean =
        (k.memberProperties.firstOrNull { it.name == name }?.getter?.call(obj) as? Boolean)
            ?: throw ContractViolation.internal("result.$name must be provided")
}
