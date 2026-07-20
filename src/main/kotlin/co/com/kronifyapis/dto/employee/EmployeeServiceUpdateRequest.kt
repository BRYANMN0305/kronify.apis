package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotEmpty

data class EmployeeServiceUpdateRequest(
    @field:NotEmpty(message = "Debe enviar al menos un servicio")
    val serviceIds: List<Long>
)
