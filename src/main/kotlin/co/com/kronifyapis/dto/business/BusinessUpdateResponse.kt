package co.com.kronifyapis.dto.business

import java.time.LocalDateTime

/**
 * DTO que devuelve la fecha de la última actualización del negocio.
 */

data class BusinessUpdateResponse(
    val updatedAt: LocalDateTime
)
