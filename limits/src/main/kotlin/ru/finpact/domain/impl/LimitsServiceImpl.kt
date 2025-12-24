package ru.finpact.domain.impl

import ru.finpact.contracts.annotations.Invariants
import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.utils.invariants.StatelessServiceInvariant
import ru.finpact.domain.LimitsService
import ru.finpact.dto.limits.LimitsProfileResponse
import ru.finpact.dto.limits.LimitsUsageResponse
import ru.finpact.infra.repository.LimitsRepository
import ru.finpact.model.LimitProfile
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset

@Invariants(StatelessServiceInvariant::class)
class LimitsServiceImpl(
    private val limitsRepository: LimitsRepository
) : LimitsService {

    override fun getLimitsProfile(ownerId: Long, callerId: Long): LimitsProfileResponse {
        if (callerId != ownerId) throw ContractViolation.notFound("limits not found")

        val profile = limitsRepository.findProfile(ownerId)
            ?: throw ContractViolation.notFound("limits profile not found")

        return profile.toDto()
    }

    override fun getLimitsUsage(ownerId: Long, callerId: Long): LimitsUsageResponse {
        if (callerId != ownerId) throw ContractViolation.notFound("limits not found")

        val profile = limitsRepository.findProfile(ownerId)
            ?: throw ContractViolation.notFound("limits profile not found")

        val today = LocalDate.now(ZoneOffset.UTC)
        val monthStart = today.withDayOfMonth(1)

        val dailyUsed = limitsRepository.getDailyUsed(ownerId, today) ?: BigDecimal.ZERO
        val monthlyUsed = limitsRepository.getMonthlyUsed(ownerId, monthStart) ?: BigDecimal.ZERO

        val dailyRemaining = maxZero(profile.daily.subtract(dailyUsed))
        val monthlyRemaining = maxZero(profile.monthly.subtract(monthlyUsed))

        return LimitsUsageResponse(
            ownerId = ownerId,
            day = today.toString(),
            dailyLimit = profile.daily.toPlainString(),
            dailyUsed = dailyUsed.toPlainString(),
            dailyRemaining = dailyRemaining.toPlainString(),
            monthStart = monthStart.toString(),
            monthlyLimit = profile.monthly.toPlainString(),
            monthlyUsed = monthlyUsed.toPlainString(),
            monthlyRemaining = monthlyRemaining.toPlainString(),
        )
    }

    private fun maxZero(v: BigDecimal): BigDecimal =
        if (v.signum() < 0) BigDecimal.ZERO else v
}

private fun LimitProfile.toDto() = LimitsProfileResponse(
    ownerId = ownerId,
    perTxn = perTxn.toPlainString(),
    daily = daily.toPlainString(),
    monthly = monthly.toPlainString(),
    currencies = currencies,
)
