package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employeeInvitation.StatusType
import co.com.kronifyapis.dto.employeeInvitation.InvitationResponse
import co.com.kronifyapis.model.EmployeeInvitation
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeInvitationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmployeeInvitationService(
    private val invitationRepository: EmployeeInvitationRepository,
    private val businessRepository: BusinessRepository,
    private val emailService: EmailService
) {
    companion object {
        const val INVITATION_TTL_DAYS = 7L
    }

    @Transactional
    fun createInvitation(businessId: UUID, email: String): InvitationResponse {
        val business =
            businessRepository.findById(businessId).orElseThrow { NoSuchElementException("Negocio no encontrado") }
        val normalizedEmail = email.trim().lowercase()

        val existing = invitationRepository.findByBusiness_BusinessIdAndEmailAndStatus(
            businessId, normalizedEmail, StatusType.PENDING
        )
        require(existing == null) { "Ya existe una invitación pendiente para este correo" }

        val invitation = EmployeeInvitation().apply {
            this.business = business
            this.email = normalizedEmail
            this.token = UUID.randomUUID().toString()
            this.expiresAt = LocalDateTime.now().plusDays(INVITATION_TTL_DAYS)
            this.status = StatusType.PENDING
        }

        val saved = invitationRepository.save(invitation)
        emailService.sendInvitationEmail(saved.email, saved.token, business.name)
        return saved.toResponse()
    }

    fun resendInvitation(invitationId: UUID): InvitationResponse {
        val invitation = getPendingOrThrow(invitationId)
        invitation.token = UUID.randomUUID().toString()
        invitation.expiresAt = LocalDateTime.now().plusDays(INVITATION_TTL_DAYS)
        val saved = invitationRepository.save(invitation)
        emailService.sendInvitationEmail(
            to = saved.email, token = saved.token, businessName = invitation.business!!.name
        )
        return saved.toResponse()
    }

    fun cancelInvitation(invitationId: UUID): InvitationResponse {
        val invitation = getPendingOrThrow(invitationId)
        invitation.status = StatusType.CANCELLED
        return invitationRepository.save(invitation).toResponse()
    }

    private fun getPendingOrThrow(invitationId: UUID): EmployeeInvitation {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { NoSuchElementException("Invitación no encontrada") }
        check(invitation.status == StatusType.PENDING) { "La invitación no está pendiente" }
        return invitation
    }

    fun listInvitations(businessId: UUID): List<InvitationResponse> {
        return invitationRepository.findAllByBusiness_BusinessId(businessId).map { it.toResponse() }
    }

    private fun EmployeeInvitation.toResponse(): InvitationResponse {
        return InvitationResponse(
            invitationId = requireNotNull(invitationId),
            businessId = requireNotNull(business?.businessId),
            email = email,
            status = status,
            expiresAt = expiresAt,
            createdAt = createdAt
        )
    }
}
