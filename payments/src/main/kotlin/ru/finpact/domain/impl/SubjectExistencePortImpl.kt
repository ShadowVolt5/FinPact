package ru.finpact.domain.impl

import ru.finpact.contracts.ports.SubjectExistencePort
import ru.finpact.infra.repository.UsersRepository

class SubjectExistencePortImpl(
    private val usersRepository: UsersRepository
) : SubjectExistencePort {

    override fun subjectExists(userId: Long): Boolean =
        usersRepository.existsById(userId)
}
