package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotNull

data class OwnerEmployeeToggleRequest(
    @field:NotNull(message = "El valor de activación es obligatorio")
    val enabled: Boolean
)
