package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.appointment.AppointmentCreateRequest
import co.com.kronifyapis.dto.appointment.AppointmentResponse
import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.service.AppointmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/appointments")
class PublicAppointmentController(
    private val appointmentService: AppointmentService
) {

    @PostMapping("/")
    fun createAppointment(
        @Valid @RequestBody request: AppointmentCreateRequest,
        @AuthenticationPrincipal user: AuthenticatedUser?
    ): ResponseEntity<AppointmentResponse> {
        val businessId = request.businessId
            ?: throw IllegalArgumentException("businessId es requerido")

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createAppointmentByClient(user?.userId, businessId, request))
    }

    @PostMapping("/cancel/{appointmentId}")
    fun cancelOwnAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable appointmentId: Long
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.ok(
            appointmentService.cancelOwnAppointment(user.userId, appointmentId)
        )
    }
}
