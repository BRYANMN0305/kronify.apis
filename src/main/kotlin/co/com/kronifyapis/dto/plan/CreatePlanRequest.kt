package co.com.kronifyapis.dto.plan

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class CreatePlanRequest(
    @field:NotBlank
    val name: String,

    val displayName: String? = null,

    val description: String? = null,

    @field:PositiveOrZero
    val monthlyPriceCents: Int = 0,

    @field:PositiveOrZero
    val serviceLimit: Int? = null,

    @field:PositiveOrZero
    val monthlyAppointmentLimit: Int? = null,

    @field:PositiveOrZero
    val employeeLimit: Int? = null,

    val requiresActivationCode: Boolean = false
)
