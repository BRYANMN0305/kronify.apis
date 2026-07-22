package co.com.kronifyapis.service

import co.com.kronifyapis.dto.user.UserChangePasswordRequest
import co.com.kronifyapis.dto.user.UserChangePasswordResponse
import co.com.kronifyapis.dto.user.UserProfileResponse
import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


/**
 * Servicio para gestionar datos del usuario.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    /**
     * Actualiza los datos de perfil del usuario.
     */
    @Transactional
    fun updateUser(userId: Long, request: UserUpdateRequest): UserUpdateResponse {
        val user =
            userRepository.findByUserId(userId) ?: throw ResourceNotFoundException("Usuario no encontrado")
        request.name?.let { user.name = it }
        request.lastName?.let { user.lastName = it}
        request.phoneNumber?.let { user.phoneNumber = it.trim() }

        return userRepository.save(user).toResponse()
    }

    private fun User.toProfileResponse(): UserProfileResponse {
        return UserProfileResponse(
            userId = requireNotNull(userId),
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,

            profileType = profileType,
            active = active,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun User.toResponse(): UserUpdateResponse {
        return UserUpdateResponse(
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            updatedAt = updatedAt,
        )
    }

    /**
     * Obtiene el perfil completo del usuario.
     */
    @Transactional(readOnly = true)
    fun getProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return user.toProfileResponse()
    }

    /**
     * Cambia la contrasena del usuario. Primero verifica que la contrasena
     * actual sea correcta antes de actualizarla.
     */
    @Transactional
    fun changePassword(userId: Long, request: UserChangePasswordRequest): UserChangePasswordResponse {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw BadRequestException("Contraseña actual incorrecta")
        }

        user.passwordHash = requireNotNull(passwordEncoder.encode(request.newPassword))

        return UserChangePasswordResponse(
            updatedAt = user.updatedAt,
            message = "Contraseña actualizada correctamente"
        )
    }
}
