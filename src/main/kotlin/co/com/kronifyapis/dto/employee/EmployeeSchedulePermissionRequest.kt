package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotNull

data class EmployeeSchedulePermissionRequest(
    @field:NotNull(message = "El valor de autogestión es obligatorio")
    val selfManagedSchedule: Boolean
)
