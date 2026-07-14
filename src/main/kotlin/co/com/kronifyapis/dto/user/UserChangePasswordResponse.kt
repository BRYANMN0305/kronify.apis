package co.com.kronifyapis.dto.user

import java.time.LocalDateTime
import java.util.UUID

data class UserChangePasswordResponse(
    val userId: UUID,
    val updatedAt: LocalDateTime,
    val message: String
)