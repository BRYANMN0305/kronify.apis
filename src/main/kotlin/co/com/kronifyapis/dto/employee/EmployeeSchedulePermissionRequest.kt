package co.com.kronifyapis.dto.employee


import jakarta.validation.constraints.NotNull

/**
 * DTO que recibe si un empleado puede autogestionar su horario o no.
 */

data class EmployeeSchedulePermissionRequest(
    @field:NotNull(message = "El valor de autogestión es obligatorio")
    val selfManagedSchedule: Boolean
)
