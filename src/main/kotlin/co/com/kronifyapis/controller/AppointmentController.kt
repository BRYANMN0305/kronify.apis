package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.appointment.AppointmentCreateRequest
import co.com.kronifyapis.dto.appointment.AppointmentRescheduleRequest
import co.com.kronifyapis.dto.appointment.AppointmentResponse
import co.com.kronifyapis.dto.appointment.AppointmentStatusUpdateRequest
import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.service.AppointmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/business/appointments")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @PostMapping("/")
    fun createAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: AppointmentCreateRequest
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createAppointmentByBusiness(user.userId, request))
    }

    @GetMapping("/")
    fun listAppointments(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<AppointmentResponse>> {
        return ResponseEntity.ok(appointmentService.listAppointments(user.userId))
    }

    @GetMapping("/{appointmentId}")
    fun getAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable appointmentId: Long
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.ok(appointmentService.getAppointment(user.userId, appointmentId))
    }

    @PatchMapping("/{appointmentId}/status")
    fun updateAppointmentStatus(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable appointmentId: Long,
        @Valid @RequestBody request: AppointmentStatusUpdateRequest
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.ok(
            appointmentService.updateAppointmentStatus(user.userId, appointmentId, request)
        )
    }

    @PatchMapping("/{appointmentId}/reschedule")
    fun rescheduleAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable appointmentId: Long,
        @Valid @RequestBody request: AppointmentRescheduleRequest
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.ok(
            appointmentService.rescheduleAppointment(user.userId, appointmentId, request)
        )
    }
}
