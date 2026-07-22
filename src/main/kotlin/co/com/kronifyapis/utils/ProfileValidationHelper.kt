package co.com.kronifyapis.utils

import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.User
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Component

/**
 * Helper para validar que un usuario tenga el perfil adecuado
 * antes de ejecutar una operación restringida.
 * Lanza excepciones si el usuario no existe o no tiene el perfil esperado.
 */
@Component
class ProfileValidationHelper(
    private val userRepository: UserRepository
) {
    /**
     * Busca al usuario por su ID y verifica que tenga alguno de los perfiles esperados.
     * @return El usuario si existe y tiene el perfil correcto.
     * @throws ResourceNotFoundException si el usuario no existe.
     * @throws ForbiddenOperationException si el perfil no es el esperado.
     */
    fun requireProfileType(userId: Long, vararg expected: ProfileType): User {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        requireProfileType(user, *expected)
        return user
    }

    /**
     * Verifica que un usuario ya cargado tenga uno de los perfiles esperados.
     * @throws ForbiddenOperationException si el perfil no coincide.
     */
    fun requireProfileType(user: User, vararg expected: ProfileType) {
        if (user.profileType !in expected) {
            throw ForbiddenOperationException(
                "Se requiere perfil ${expected.joinToString(" o ")}. " +
                    "Perfil actual: ${user.profileType}"
            )
        }
    }

    /**
     * Atajo para requerir perfil BUSINESS.
     */
    fun requireBusiness(userId: Long): User = requireProfileType(userId, ProfileType.BUSINESS)

    /**
     * Atajo para requerir perfil CLIENT.
     */
    fun requireClient(userId: Long): User = requireProfileType(userId, ProfileType.CLIENT)
}
