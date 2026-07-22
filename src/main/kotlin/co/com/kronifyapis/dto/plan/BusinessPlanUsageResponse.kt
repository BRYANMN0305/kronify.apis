package co.com.kronifyapis.dto.plan

import java.time.LocalDateTime

/**
 * DTO que devuelve el uso actual del plan de un negocio.
 */

data class BusinessPlanUsageResponse(
    val plan: PlanResponse,
    val active: Boolean,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val serviceCount: Long,
    val currentMonthAppointmentCount: Long,
    val employeeCount: Long,
    val serviceLimitReached: Boolean,
    val appointmentLimitReached: Boolean,
    val employeeLimitReached: Boolean
)
