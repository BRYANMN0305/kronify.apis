package co.com.kronifyapis.dto.auth


import co.com.kronifyapis.model.enums.ProfileType

/**
 * DTO que representa al usuario autenticado dentro del sistema.
 */

data class AuthenticatedUser(
    val userId: Long,
    val email: String,
    val profileType: ProfileType,
    val businessId: Long? = null,
    val slug: String? = null
)
