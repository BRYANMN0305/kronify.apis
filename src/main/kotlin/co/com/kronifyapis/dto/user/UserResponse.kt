package co.com.kronifyapis.dto.user

import java.time.LocalDateTime

data class UserResponse(
    val userId: Long,
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val email: String,
    val verifiedEmail: Boolean,
    val profileType: ProfileType,
    val active: Boolean,
    val createdAt: LocalDateTime,
)

