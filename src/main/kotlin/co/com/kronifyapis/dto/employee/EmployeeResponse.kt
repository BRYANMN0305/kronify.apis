package co.com.kronifyapis.dto.employee

import java.time.LocalDateTime

data class EmployeeResponse(
    val employeeId: Long,
    val name: String,
    val lastName: String,
    val email: String,
    val owner: Boolean,
    val selfManagedSchedule: Boolean,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
