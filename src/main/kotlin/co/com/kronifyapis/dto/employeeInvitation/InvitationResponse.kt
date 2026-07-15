package co.com.kronifyapis.dto.employeeInvitation

import java.time.LocalDateTime
import java.util.UUID

data class InvitationResponse(
    val invitationId: UUID,
    val businessId: UUID,
    val email: String,
    val status: StatusType,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)