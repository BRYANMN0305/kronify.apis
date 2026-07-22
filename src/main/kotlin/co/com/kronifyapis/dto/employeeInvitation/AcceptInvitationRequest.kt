package co.com.kronifyapis.dto.employeeInvitation

import jakarta.validation.constraints.NotBlank

/**
 * DTO que recibe el token para aceptar una invitación de empleado.
 */

data class AcceptInvitationRequest(
    @field:NotBlank
    val token: String
)
