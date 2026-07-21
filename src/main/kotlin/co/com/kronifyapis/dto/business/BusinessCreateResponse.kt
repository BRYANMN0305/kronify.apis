package co.com.kronifyapis.dto.business


import java.time.LocalDateTime

/**
 * DTO que devuelve la fecha de creación después de registrar un negocio.
 */

data class BusinessCreateResponse(
    val createdAt: LocalDateTime
)
