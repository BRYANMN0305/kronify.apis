package co.com.kronifyapis.dto.employeeInvitation

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * DTO que recibe el correo del empleado a invitar.
 */

data class InvitationRequest(
    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "El email no es válido")
    val email: String
)
