package co.com.kronifyapis.dto.employee

/**
 * DTO que recibe los datos para actualizar un empleado.
 * Ambos campos son opcionales: solo se actualizan los que se envían.
 */

data class EmployeeUpdateRequest(
    val selfManagedSchedule: Boolean? = null,
    val active: Boolean? = null
)
