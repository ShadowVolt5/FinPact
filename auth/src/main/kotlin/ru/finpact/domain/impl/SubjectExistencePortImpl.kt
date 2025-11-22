package ru.finpact.domain.impl

import ru.finpact.contracts.ports.SubjectExistencePort
import ru.finpact.infra.repository.AuthRepository

class SubjectExistencePortImpl(
    private val authRepository: AuthRepository
) : SubjectExistencePort {

    override fun subjectExists(userId: Long): Boolean =
        authRepository.findUserById(userId) != null
}
