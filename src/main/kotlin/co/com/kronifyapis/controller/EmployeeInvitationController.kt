package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.employeeInvitation.InvitationRequest
import co.com.kronifyapis.dto.employeeInvitation.InvitationResponse
import co.com.kronifyapis.service.EmployeeInvitationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/invitations")
class EmployeeInvitationController(
    private val invitationService: EmployeeInvitationService
) {

    @PostMapping("/")
    fun createInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: InvitationRequest
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(invitationService.createInvitation(user.userId, businessId, request.email))
    }

    @GetMapping("/")
    fun listInvitations(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID
    ): ResponseEntity<List<InvitationResponse>> {
        return ResponseEntity.ok(invitationService.listInvitations(user.userId, businessId))
    }

    @PostMapping("/{invitationId}/resend")
    fun resendInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable invitationId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.resendInvitation(user.userId, businessId, invitationId))
    }

    @PostMapping("/{invitationId}/cancel")
    fun cancelInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable invitationId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.cancelInvitation(user.userId, businessId, invitationId))
    }

}
