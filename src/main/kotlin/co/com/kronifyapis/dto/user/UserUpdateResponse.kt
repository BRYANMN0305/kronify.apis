package co.com.kronifyapis.dto.user

import java.time.LocalDateTime

data class UserUpdateResponse(
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val updatedAt: LocalDateTime
)
