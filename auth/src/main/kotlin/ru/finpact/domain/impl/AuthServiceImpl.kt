package ru.finpact.domain.impl

import ru.finpact.contracts.core.ContractViolation
import ru.finpact.contracts.ports.EmailUniquenessPort
import ru.finpact.crypto.PasswordHasher
import ru.finpact.domain.AuthService
import ru.finpact.dto.login.LoginRequest
import ru.finpact.dto.login.LoginResponse
import ru.finpact.dto.login.UserView
import ru.finpact.dto.register.RegisterRequest
import ru.finpact.dto.register.RegisterResponse
import ru.finpact.dto.sql.PgSqlState
import ru.finpact.infra.repository.AuthRepository
import ru.finpact.jwt.JwtService
import java.sql.SQLException

class AuthServiceImpl(
    private val authRepository: AuthRepository
) : AuthService, EmailUniquenessPort {

    override fun register(request: RegisterRequest): RegisterResponse {
        val email = request.email.trim()
        val hash = PasswordHasher.hash(request.password)

        return try {
            val id = authRepository.insertUser(
                email = email,
                firstName = request.firstName,
                middleName = request.middleName,
                lastName = request.lastName,
                passwordHash = hash
            )
            RegisterResponse(
                id = id,
                email = email,
                firstName = request.firstName,
                middleName = request.middleName,
                lastName = request.lastName
            )
        } catch (e: SQLException) {
            if (e.sqlState == PgSqlState.UNIQUE_VIOLATION) {
                throw ContractViolation.conflict("email is already taken")
            }
            throw e
        }
    }

    override fun isEmailFree(email: String): Boolean =
        authRepository.findUserIdByEmail(email.trim()) == null

    override fun login(request: LoginRequest): LoginResponse {
        val email = request.email.trim()
        val user = authRepository.findUserByEmail(email)
            ?: throw ContractViolation.unauthorized("invalid credentials")

        val isOk = PasswordHasher.verify(request.password, user.password)
        if (!isOk) throw ContractViolation.unauthorized("invalid credentials")

        val token = JwtService.generateJwtToken(
            userId = user.id,
            email = user.email
        )

        return LoginResponse(
            user = UserView(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                middleName = user.middleName,
                lastName = user.lastName
            ),
            token = token
        )
    }
}
