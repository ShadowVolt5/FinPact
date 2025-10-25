package ru.finpact.contracts.utils

import ru.finpact.contracts.core.*

class ResultNotNull : Postcondition {
    override fun verify(ctx: ContractContext) {
        if (ctx.result == null) throw ContractViolation("result must not be null")
    }
}
