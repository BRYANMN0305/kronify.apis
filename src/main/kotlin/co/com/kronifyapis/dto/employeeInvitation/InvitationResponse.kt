package co.com.kronifyapis.dto.employeeInvitation

import java.time.LocalDateTime

data class InvitationResponse(
    val invitationId: Long,
    val businessId: Long,
    val email: String,
    val status: StatusType,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)