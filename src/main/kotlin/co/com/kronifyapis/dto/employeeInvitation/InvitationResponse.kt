package co.com.kronifyapis.dto.employeeInvitation

import co.com.kronifyapis.model.enums.StatusType
import java.time.LocalDateTime

/**
 * DTO que devuelve la información de una invitación de empleado.
 */

data class InvitationResponse(
    val invitationId: Long,
    val businessId: Long,
    val email: String,
    val status: StatusType,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime
)