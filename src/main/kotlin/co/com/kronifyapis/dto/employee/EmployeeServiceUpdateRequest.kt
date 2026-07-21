package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotEmpty

/**
 * DTO que recibe la lista actualizada de servicios para un empleado.
 */

data class EmployeeServiceUpdateRequest(
    @field:NotEmpty(message = "Debe enviar al menos un servicio")
    val serviceIds: List<Long>
)
