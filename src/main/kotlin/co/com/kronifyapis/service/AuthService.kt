package co.com.kronifyapis.service

import co.com.kronifyapis.dto.auth.LoginRequest
import co.com.kronifyapis.dto.auth.TokenResponse
import co.com.kronifyapis.dto.user.ProfileType
import co.com.kronifyapis.dto.auth.UserRegisterRequest
import co.com.kronifyapis.dto.user.UserResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.TypeErrorException
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun register(request: UserRegisterRequest): UserResponse {
        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("El email ya se encuentra registrado")
        }

        if (!request.name.matches(Regex("^[a-zA-Z ]+$"))) {
            throw TypeErrorException("El nombre solo puede contener letras")
        }

        if (!request.lastName.matches(Regex("^[a-zA-Z ]+$"))) {
            throw TypeErrorException("El apellido solo puede contener letras")
        }

        if (request.passwordHash.length < 8) {
            throw BadRequestException("La contraseña debe tener al menos 8 caracteres")
        }

        if (request.profileType != "CLIENT" && request.profileType != "BUSINESS") {
            throw BadRequestException("El tipo de perfil debe ser 'CLIENT' o 'BUSINESS'")
        }

        val user = User(
            name = request.name,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber.trim(),
            email = email,
            passwordHash = requireNotNull(passwordEncoder.encode(request.passwordHash)),
            profileType = ProfileType.valueOf(request.profileType),
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
            profileType = profileType,
            active = active,
            createdAt = createdAt,
        )
    }

    fun login(request: LoginRequest): TokenResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("Correo o contraseña incorrectas")

        if (!user.active || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException("Correo o contraseña incorrectas")
        }

        val token = jwtService.generateToken(user)
        return user.toTokenResponse(token, jwtService.getExpirationSeconds())
    }

    private fun User.toTokenResponse(token: String, expiresIn: Long): TokenResponse {
        return TokenResponse(
            accessToken = token,
            expiresIn = expiresIn
        )
    }
}
