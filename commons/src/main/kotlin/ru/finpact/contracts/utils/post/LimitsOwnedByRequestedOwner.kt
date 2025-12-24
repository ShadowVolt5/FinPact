package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition
import ru.finpact.contracts.core.arg
import kotlin.reflect.full.memberProperties

class LimitsOwnedByRequestedOwner : Postcondition {

    override fun verify(ctx: ContractContext) {
        val ownerId = ctx.arg<Long>("ownerId")
        val res = ctx.result ?: throw ContractViolation.internal("result must not be null")

        val prop = res::class.memberProperties.firstOrNull { it.name == "ownerId" }
            ?: throw ContractViolation.internal("result.ownerId property is missing")

        val resOwnerId = prop.getter.call(res) as? Long
            ?: throw ContractViolation.internal("result.ownerId must be Long")

        if (resOwnerId != ownerId) {
            throw ContractViolation.notFound("limits not found")
        }
    }
}
