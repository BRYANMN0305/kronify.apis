package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.appointment.AppointmentCreateRequest
import co.com.kronifyapis.dto.appointment.AppointmentRescheduleRequest
import co.com.kronifyapis.dto.appointment.AppointmentResponse
import co.com.kronifyapis.dto.appointment.AppointmentStatusUpdateRequest
import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.model.enums.AppointmentOrigin
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.service.AppointmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Controlador para gestionar las citas desde el negocio.
 * Aquí el dueño o empleados del negocio pueden crear, listar, consultar,
 * cambiar el estado y reagendar citas.
 */
@RestController
@RequestMapping("/business/appointments")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    /**
     * Crea una nueva cita como negocio.
     * El usuario autenticado debe tener perfil BUSINESS.
     */
    @PostMapping("/")
    fun createAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: AppointmentCreateRequest
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(appointmentService.createAppointmentByBusiness(user.userId, request))
    }

    /**
     * Lista todas las citas del negocio al que pertenece el usuario autenticado.
     */
    @GetMapping("/")
    fun listAppointments(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<AppointmentResponse>> {
        return ResponseEntity.ok(appointmentService.listAppointments(user.userId))
    }

    /**
     * Obtiene la agenda de un empleado en un rango de fechas.
     * Si no se pasa employeeId, devuelve la agenda de todo el negocio.
     */
    @GetMapping("/agenda")
    fun getAgenda(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam(required = false) serviceId: Long?,
        @RequestParam(required = false) status: AppointmentStatus?,
        @RequestParam(required = false) origin: AppointmentOrigin?
    ): ResponseEntity<List<AppointmentResponse>> {
        return ResponseEntity.ok(
            appointmentService.getCalendarAppointments(
                userId = user.userId,
                startDate = startDate,
                endDate = endDate,
                employeeId = employeeId,
                serviceId = serviceId,
                status = status,
                origin = origin
            )
        )
    }

    /**
     * Devuelve los detalles de una cita específica del negocio.
     */
    @GetMapping("/{appointmentId}")
    fun getAppointment(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable appointmentId: Long
    ): ResponseEntity<AppointmentResponse> {
        return ResponseEntity.ok(appointmentService.getAppointment(user.userId, appointmentId))
    }

    /**
     * Actualiza el estado de una cita.
     */
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

    /**
     * Reagenda una cita para una nueva fecha u hora.
     */
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
