package co.com.kronifyapis.dto.plan

import jakarta.validation.constraints.PositiveOrZero

data class UpdatePlanRequest(
    val name: String? = null,

    val displayName: String? = null,

    val description: String? = null,

    @field:PositiveOrZero
    val monthlyPriceCents: Int? = null,

    @field:PositiveOrZero
    val serviceLimit: Int? = null,

    @field:PositiveOrZero
    val monthlyAppointmentLimit: Int? = null,

    @field:PositiveOrZero
    val employeeLimit: Int? = null,

    val requiresActivationCode: Boolean? = null
)
