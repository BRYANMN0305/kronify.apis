package co.com.kronifyapis.dto.services


import java.time.LocalDateTime

/**
 * DTO que devuelve la información de un servicio.
 */

data class ServiceResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double?,
    val durationMinutes: Int,
    val active: Boolean,
    val createdAt: LocalDateTime
)
