package co.com.kronifyapis.dto.user

import java.time.LocalDateTime

/**
 * DTO que devuelve los datos actualizados del usuario después de modificarlos.
 */

data class UserUpdateResponse(
    val name: String,
    val lastName: String,
    val phoneNumber: String?,
    val updatedAt: LocalDateTime
)
