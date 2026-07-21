package co.com.kronifyapis.dto.user

import java.time.LocalDateTime

/**
 * DTO que confirma que la contraseña fue actualizada exitosamente.
 */

data class UserChangePasswordResponse(
    val updatedAt: LocalDateTime,
    val message: String
)
