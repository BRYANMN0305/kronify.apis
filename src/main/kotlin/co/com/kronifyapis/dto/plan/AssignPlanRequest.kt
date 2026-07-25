package co.com.kronifyapis.dto.plan

import jakarta.validation.constraints.NotNull

/**
 * DTO que recibe el ID del plan a asignar a un negocio,
 * y opcionalmente un código de activación si el plan lo requiere.
 */

data class AssignPlanRequest(

    @field:NotNull
    val planId: Long,

    val activationCode: String? = null
)
