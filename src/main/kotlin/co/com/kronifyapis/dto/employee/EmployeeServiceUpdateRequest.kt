package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class EmployeeServiceUpdateRequest(
    @field:NotEmpty(message = "Debe enviar al menos un servicio")
    val serviceIds: List<UUID>
)
