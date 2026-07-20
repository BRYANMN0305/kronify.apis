package co.com.kronifyapis.dto.user

import java.time.LocalDateTime

data class UserChangePasswordResponse(
    val updatedAt: LocalDateTime,
    val message: String
)
