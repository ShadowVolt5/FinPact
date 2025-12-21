package ru.finpact.contracts.utils.invariants

import ru.finpact.contracts.core.ContractContext
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.core.InvariantRule
import java.lang.reflect.Modifier

class StatelessServiceInvariant : InvariantRule {
    override fun verify(ctx: ContractContext) {
        val cls = ctx.target.javaClass
        val hasNonFinal = cls.declaredFields
            .asSequence()
            .filterNot { it.isSynthetic }
            .any { !Modifier.isFinal(it.modifiers) }

        if (hasNonFinal) {
            throw ContractViolation.internal("service must be stateless")
        }
    }
}
