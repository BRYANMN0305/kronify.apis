package co.com.kronifyapis.config

import co.com.kronifyapis.dto.user.ProfileType
import java.util.UUID

data class AuthenticatedUser(
    val userId: UUID,
    val email: String,
    val profileType: ProfileType
)