package co.com.kronifyapis.service

import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.exception.TypeError
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun updateUser(userId: UUID, request: UserUpdateRequest): UserUpdateResponse {
        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            }

        request.name?.let { newName ->
            if (newName.isBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty")
            }

            if (newName.matches(Regex("[^a-zA-Z ]"))) {
                throw TypeError("Name must contain only letters")
            }
            user.name = newName
        }
        request.lastName?.let { newLastName ->
            if (newLastName.isBlank()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Last Name cannot be empty")
            }

            if (newLastName.matches(Regex("[^a-zA-Z ]"))) {
                throw TypeError("Last Name must contain only letters")
            }
            user.lastName = newLastName
        }

        request.phoneNumber?.let { user.phoneNumber = it }

        return userRepository.save(user).toResponse()
    }

    private fun User.toResponse(): UserUpdateResponse {
        return UserUpdateResponse(
            userId = requireNotNull(userId),
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            updatedAt = updatedAt,
        )
    }
}