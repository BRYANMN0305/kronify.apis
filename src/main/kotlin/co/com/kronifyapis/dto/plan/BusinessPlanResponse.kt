package co.com.kronifyapis.dto.plan

import java.time.LocalDateTime

data class BusinessPlanResponse(
    val plan: PlanResponse,
    val active: Boolean,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?
)
