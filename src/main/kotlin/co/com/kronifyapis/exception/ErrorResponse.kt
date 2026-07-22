package co.com.kronifyapis.exception

import java.time.Instant

/**
 * DTO estándar para devolver errores al cliente.
 * Incluye timestamp, código HTTP, nombre del error, mensaje y la ruta consultada.
 */
data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String? = null
)
