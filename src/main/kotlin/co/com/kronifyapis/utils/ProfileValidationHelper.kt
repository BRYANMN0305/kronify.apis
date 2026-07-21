package co.com.kronifyapis.utils

import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.User
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class ProfileValidationHelper(
    private val userRepository: UserRepository
) {
    fun requireProfileType(userId: Long, vararg expected: ProfileType): User {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        requireProfileType(user, *expected)
        return user
    }

    fun requireProfileType(user: User, vararg expected: ProfileType) {
        if (user.profileType !in expected) {
            throw ForbiddenOperationException(
                "Se requiere perfil ${expected.joinToString(" o ")}. " +
                    "Perfil actual: ${user.profileType}"
            )
        }
    }

    fun requireBusiness(userId: Long): User = requireProfileType(userId, ProfileType.BUSINESS)

    fun requireClient(userId: Long): User = requireProfileType(userId, ProfileType.CLIENT)
}
