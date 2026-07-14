package co.com.kronifyapis.dto.user

import java.time.LocalDateTime
import java.util.UUID

data class UserUpdateResponse(
    val userId: UUID,
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val updatedAt: LocalDateTime
)