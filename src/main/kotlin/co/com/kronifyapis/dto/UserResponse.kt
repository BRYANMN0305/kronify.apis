package co.com.kronifyapis.dto

import java.time.LocalDateTime
import java.util.UUID

data class UserResponse(
    val userId: UUID,
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val email: String,
    val verifiedEmail: Boolean,
    val profileType: ProfileType,
    val active: Boolean,
    val createdAt: LocalDateTime,
)
