package co.com.kronifyapis.dto.employeeInvitation

import jakarta.validation.constraints.NotBlank

data class AcceptInvitationRequest(
    @field:NotBlank
    val token: String
)
