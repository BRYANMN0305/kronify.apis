package co.com.kronifyapis.dto.user

import co.com.kronifyapis.model.enums.ProfileType
import java.time.LocalDateTime

/**
 * DTO que devuelve la información de un usuario.
 */

data class UserResponse(
    val userId: Long,
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val email: String,

    val profileType: ProfileType,
    val active: Boolean,
    val createdAt: LocalDateTime,
)

