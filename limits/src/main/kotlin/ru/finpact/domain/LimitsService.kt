package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.post.LimitsOwnedByRequestedOwner
import ru.finpact.contracts.utils.post.LimitsProfileResponseValid
import ru.finpact.contracts.utils.post.LimitsUsageResponseValid
import ru.finpact.contracts.utils.post.ResultNotNull
import ru.finpact.contracts.utils.pre.CallerIdPositive
import ru.finpact.contracts.utils.pre.OwnerIdPositive
import ru.finpact.dto.limits.LimitsProfileResponse
import ru.finpact.dto.limits.LimitsUsageResponse

interface LimitsService {

    @Pre(
        OwnerIdPositive::class,
        CallerIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        LimitsOwnedByRequestedOwner::class,
        LimitsProfileResponseValid::class,
    )
    fun getLimitsProfile(ownerId: Long, callerId: Long): LimitsProfileResponse

    @Pre(
        OwnerIdPositive::class,
        CallerIdPositive::class,
    )
    @Post(
        ResultNotNull::class,
        LimitsOwnedByRequestedOwner::class,
        LimitsUsageResponseValid::class,
    )
    fun getLimitsUsage(ownerId: Long, callerId: Long): LimitsUsageResponse
}
