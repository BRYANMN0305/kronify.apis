package co.com.kronifyapis.dto.auth

import co.com.kronifyapis.dto.user.ProfileType

data class AuthenticatedUser(
    val userId: Long,
    val email: String,
    val profileType: ProfileType,
    val businessId: Long? = null,
    val slug: String? = null
)
