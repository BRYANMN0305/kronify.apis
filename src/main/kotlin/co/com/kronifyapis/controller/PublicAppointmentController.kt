package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.appointment.AppointmentAutofillResponse
import co.com.kronifyapis.dto.appointment.AppointmentCreateRequest
import co.com.kronifyapis.dto.appointment.AppointmentResponse
import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.service.AppointmentService
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
 * Controlador público para la gestión de citas desde el lado del cliente.
 */
@RestController
@RequestMapping("/appointments")
class PublicAppointmentController(
    private val appointmentService: AppointmentService
) {

    /**
     * Obtiene información de autocompletado para agendar una cita
     */
    @GetMapping("/autofill")
    fun getAutofill(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<AppointmentAutofillResponse> {
        return ResponseEntity.ok(appointmentService.getBookingAutofill(user.userId))
    }

    /**
     * Crea una cita como cliente. No requiere autenticación.
     */
    @PostMapping("/")
    fun createAppointment(
        @Valid @RequestBody request: AppointmentCreateRequest,
        @AuthenticationPrincipal user: AuthenticatedUser?
    ): ResponseEntity<AppointmentResponse> {
        // businessId es obligatorio para identificar a qué negocio se agenda
        val businessId = request.businessId
            ?: throw BadRequestException("businessId es requerido")

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createAppointmentByClient(user?.userId, businessId, request))
    }

    /**
     * Cancela una cita propia del cliente autenticado.
     */
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
