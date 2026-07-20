package co.com.kronifyapis.dto.plan

import java.time.LocalDateTime

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
