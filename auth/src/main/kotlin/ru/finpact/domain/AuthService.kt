package ru.finpact.domain

import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.*
import ru.finpact.dto.register.RegisterRequest
import ru.finpact.dto.register.RegisterResponse

interface AuthService {
    @Pre(
        Arg0NotNull::class,
        EmailFormat::class,
        PasswordPolicy::class,
        EmailUnique::class
    )
    @Post(ResultNotNull::class)
    fun register(request: RegisterRequest): RegisterResponse
}
