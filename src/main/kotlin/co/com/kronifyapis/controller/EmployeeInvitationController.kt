package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.employeeInvitation.CreateInvitationRequest
import co.com.kronifyapis.dto.employeeInvitation.InvitationResponse
import co.com.kronifyapis.service.EmployeeInvitationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/invitations")
class EmployeeInvitationController(
    private val invitationService: EmployeeInvitationService
) {

    @PostMapping("/")
    fun createInvitation(
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: CreateInvitationRequest
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(invitationService.createInvitation(businessId, request.email))
    }

    @GetMapping("/")
    fun listInvitations(@PathVariable businessId: UUID): ResponseEntity<List<InvitationResponse>> {
        return ResponseEntity.ok(invitationService.listInvitations(businessId))
    }

    @PostMapping("/{invitationId}/resend")
    fun resendInvitation(
        @PathVariable businessId: UUID,
        @PathVariable invitationId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.resendInvitation(invitationId))
    }

    @PostMapping("/{invitationId}/cancel")
    fun cancelInvitation(
        @PathVariable businessId: UUID,
        @PathVariable invitationId: UUID
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.cancelInvitation(invitationId))
    }

}
