package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employeeInvitation.StatusType
import co.com.kronifyapis.dto.employeeInvitation.InvitationResponse
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.EmployeeInvitation
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeInvitationRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EmployeeInvitationService(
    private val invitationRepository: EmployeeInvitationRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val planService: PlanService
) {
    companion object {
        const val INVITATION_TTL_DAYS = 7L
    }

    @Transactional
    fun createInvitation(userId: Long, email: String): InvitationResponse {
        val business = ensureUserCanManageBusiness(userId)
        val businessId = business.businessId!!

        planService.validateEmployeeLimit(businessId)

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
        //emailService.sendInvitationEmail(saved.email, saved.token, business.name)
        return saved.toResponse()
    }

    fun resendInvitation(userId: Long, invitationId: Long): InvitationResponse {
        val business = ensureUserCanManageBusiness(userId)
        val invitation = getPendingOrThrow(business.businessId!!, invitationId)
        invitation.token = UUID.randomUUID().toString()
        invitation.expiresAt = LocalDateTime.now().plusDays(INVITATION_TTL_DAYS)
        val saved = invitationRepository.save(invitation)
        emailService.sendInvitationEmail(
            to = saved.email, token = saved.token, businessName = invitation.business!!.name
        )
        return saved.toResponse()
    }

    fun cancelInvitation(userId: Long, invitationId: Long): InvitationResponse {
        val business = ensureUserCanManageBusiness(userId)
        val invitation = getPendingOrThrow(business.businessId!!, invitationId)
        invitation.status = StatusType.CANCELLED
        return invitationRepository.save(invitation).toResponse()
    }

    private fun getPendingOrThrow(businessId: Long, invitationId: Long): EmployeeInvitation {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { ResourceNotFoundException("Invitación no encontrada") }
        check(invitation.business?.businessId == businessId) { "La invitación no pertenece al negocio solicitado" }
        check(invitation.status == StatusType.PENDING) { "La invitación no está pendiente" }
        return invitation
    }

    fun listInvitations(userId: Long): List<InvitationResponse> {
        val business = ensureUserCanManageBusiness(userId)
        return invitationRepository.findAllByBusiness_BusinessId(business.businessId!!).map { it.toResponse() }
    }

    private fun ensureUserCanManageBusiness(userId: Long) =
        businessRepository.findByOwner(
            userRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Usuario no encontrado")
        ) ?: throw ForbiddenOperationException("No tiene permiso para gestionar invitaciones de este negocio")

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
