package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.employeeInvitation.AcceptInvitationRequest
import co.com.kronifyapis.dto.employeeInvitation.InvitationRequest
import co.com.kronifyapis.dto.employeeInvitation.InvitationResponse
import co.com.kronifyapis.service.EmployeeInvitationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador para gestionar invitaciones de empleados.
 */

@RestController
@RequestMapping("/business/invitations")
class EmployeeInvitationController(
    private val invitationService: EmployeeInvitationService
) {

    /**
     * Crea una invitación para que un empleado se una al negocio.
     */
    @PostMapping("/")
    fun createInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: InvitationRequest
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(invitationService.createInvitation(user.userId, request.email))
    }

    /**
     * Lista todas las invitaciones enviadas por el negocio.
     */
    @GetMapping("/")
    fun listInvitations(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<InvitationResponse>> {
        return ResponseEntity.ok(invitationService.listInvitations(user.userId))
    }

    /**
     * Reenvía una invitación que ya había sido creada.
     */
    @PostMapping("/{invitationId}/resend")
    fun resendInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable invitationId: Long
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.resendInvitation(user.userId, invitationId))
    }

    /**
     * Cancela una invitación pendiente.
     */
    @PostMapping("/{invitationId}/cancel")
    fun cancelInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable invitationId: Long
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.cancelInvitation(user.userId, invitationId))
    }

    /**
     * Acepta una invitación usando el token enviado por correo.
     */
    @PostMapping("/accept")
    fun acceptInvitation(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: AcceptInvitationRequest
    ): ResponseEntity<InvitationResponse> {
        return ResponseEntity.ok(invitationService.acceptInvitation(user.userId, request.token))
    }
}
