package co.com.kronifyapis.dto.plan

import jakarta.validation.constraints.NotNull

/**
 * DTO que recibe el ID del plan a asignar a un negocio.
 */

data class AssignPlanRequest(

    @field:NotNull
    val planId: Long
)
