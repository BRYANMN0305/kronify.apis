package co.com.kronifyapis.dto.plan

import jakarta.validation.constraints.NotNull

data class AssignPlanRequest(

    @field:NotNull
    val planId: Long
)
