
package co.com.kronifyapis.dto.plan

import java.time.LocalDateTime

/**
 * DTO que devuelve el plan asignado a un negocio, con su estado y vigencia.
 */

data class BusinessPlanResponse(
    val plan: PlanResponse,
    val active: Boolean,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?
)
