package ru.finpact.auth.service

import ru.finpact.auth.AuthPrincipal
import ru.finpact.contracts.annotations.Post
import ru.finpact.contracts.annotations.Pre
import ru.finpact.contracts.utils.pre.AuthHeaderPresent
import ru.finpact.contracts.utils.post.ResultNotNull

/**
 * Контрактный сервис аутентификации:
 * на вход — Authorization, на выход — AuthPrincipal.
 */
interface TokenAuthService {

    @Pre(AuthHeaderPresent::class)
    @Post(ResultNotNull::class)
    fun authenticate(authorizationHeader: String): AuthPrincipal
}
