package ru.finpact.contracts.utils.post

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.Postcondition

class ResultNotNull : Postcondition {
    override fun verify(ctx: ContractContext) {
        if (ctx.result == null) throw ContractViolation.internal("result must not be null")
    }
}
