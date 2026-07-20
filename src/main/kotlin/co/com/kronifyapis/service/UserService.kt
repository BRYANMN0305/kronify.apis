package co.com.kronifyapis.service

import co.com.kronifyapis.dto.user.UserChangePasswordRequest
import co.com.kronifyapis.dto.user.UserChangePasswordResponse
import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.exception.TypeErrorException
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun updateUser(userId: UUID, request: UserUpdateRequest): UserUpdateResponse {
        val user =
            userRepository.findByUserId(userId) ?: throw ResourceNotFoundException("Usuario no encontrado")
        request.name?.let { user.name = it }
        request.lastName?.let { user.lastName = it}
        request.phoneNumber?.let { user.phoneNumber = it.trim() }

        return userRepository.save(user).toResponse()
    }

    private fun User.toResponse(): UserUpdateResponse {
        return UserUpdateResponse(
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            updatedAt = updatedAt,
        )
    }

    @Transactional
    fun changePassword(userId: UUID, request: UserChangePasswordRequest): UserChangePasswordResponse {
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
