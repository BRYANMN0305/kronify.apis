package co.com.kronifyapis.dto.employee

import java.time.LocalDateTime
import java.util.UUID

data class EmployeeResponse(
    val employeeId: UUID,
    val userId: UUID,
    val businessId: UUID,
    val name: String,
    val lastName: String,
    val email: String,
    val owner: Boolean,
    val selfManagedSchedule: Boolean,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
