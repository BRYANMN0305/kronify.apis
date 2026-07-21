package co.com.kronifyapis.dto.employee

data class EmployeeUpdateRequest(
    val selfManagedSchedule: Boolean? = null,
    val active: Boolean? = null
)
