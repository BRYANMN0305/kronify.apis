package co.com.kronifyapis.service

import co.com.kronifyapis.dto.UserRegisterRequest
import co.com.kronifyapis.dto.UserResponse
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(request: UserRegisterRequest): UserResponse {
        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmail(email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already exists"
            )
        }

        val user = User(
            name = request.name,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            email = email,
            passwordHash = requireNotNull(passwordEncoder.encode(request.passwordHash)),
        )

        return userRepository.save(user).toResponse()
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse(
            userId = requireNotNull(userId),
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            verifiedEmail = verifiedEmail,
            active = active,
            createdAt = createdAt,
        )
    }
}
