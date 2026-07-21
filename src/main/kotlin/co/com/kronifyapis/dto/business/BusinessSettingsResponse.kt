package co.com.kronifyapis.dto.business


import java.time.LocalDateTime

/**
 * DTO que devuelve la configuración actual del negocio.
 */

data class BusinessSettingsResponse(
    val name: String,
    val category: String?,
    val description: String?,
    val address: String?,
    val logoUrl: String?,
    val email: String?,
    val phoneNumber: String?,
    val whatsApp: String?,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
