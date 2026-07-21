package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotNull

/**
 * DTO que activa o desactiva al dueño como empleado de su propio negocio.
 */

data class OwnerEmployeeToggleRequest(
    @field:NotNull(message = "El valor de activación es obligatorio")
    val enabled: Boolean
)
