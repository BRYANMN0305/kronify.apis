package co.com.kronifyapis.dto.auth

import java.time.LocalDateTime

/**
 * DTO que devuelve los métodos de autenticación vinculados a un usuario.
 */
data class LinkedAuthMethodResponse(
    val type: String,
    val provider: String?,
    val email: String,
    val linkedAt: LocalDateTime
)
