package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotEmpty

data class EmployeeServiceAssignRequest(
    @field:NotEmpty(message = "Debe enviar al menos un servicio")
    val serviceIds: List<Long>
)
