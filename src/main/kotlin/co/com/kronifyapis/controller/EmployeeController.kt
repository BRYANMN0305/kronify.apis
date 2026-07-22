package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
import co.com.kronifyapis.dto.employee.EmployeeUpdateRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockResponse
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleResponse
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.service.EmployeeService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable

/**
 * Controlador para gestionar empleados del negocio.
 */
@RestController
@RequestMapping("/business/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    /**
     * Lista todos los empleados del negocio del usuario autenticado.
     */
    @GetMapping("/")
    fun listEmployees(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<EmployeeResponse>> {
        return ResponseEntity.ok(employeeService.listEmployees(user.userId))
    }

    /**
     * Actualiza el permiso de un empleado para gestionar su propio horario.
     */
    @PatchMapping("/{employeeId}/schedule-permission")
    fun updateSchedulePermission(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: EmployeeSchedulePermissionRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(
            employeeService.updateEmployeeSchedulePermission(user.userId, employeeId, request)
        )
    }

    /**
     * Activa o desactiva el perfil BUSINESS de un usuario administrador.
     * Sirve para alternar la condición de "dueño" del negocio.
     */
    @PostMapping("/owner/toggle")
    fun toggleOwnerEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: OwnerEmployeeToggleRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(employeeService.toggleOwnerEmployee(user.userId, request))
    }

    /**
     * Edita los datos de un empleado.
     */
    @PatchMapping("/{employeeId}/edit")
    fun updateEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: EmployeeUpdateRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(employeeService.updateEmployee(user.userId, employeeId, request))
    }

    /**
     * Desactiva a un empleado para que ya no pueda recibir citas.
     */
    @PatchMapping("/{employeeId}/deactivate")
    fun deactivateEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(employeeService.deactivateEmployee(user.userId, employeeId))
    }

    /**
     * Elimina a un empleado del negocio.
     */
    @DeleteMapping("/{employeeId}")
    fun deleteEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<Void> {
        employeeService.deleteEmployee(user.userId, employeeId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Lista los servicios que ofrece un empleado en particular.
     */
    @GetMapping("/{employeeId}/services")
    fun listEmployeeServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(employeeService.listEmployeeServices(user.userId, employeeId))
    }

    /**
     * Asigna o reemplaza los servicios que puede realizar un empleado.
     */
    @PatchMapping("/{employeeId}/services")
    fun updateEmployeeServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: EmployeeServiceUpdateRequest
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(
            employeeService.updateEmployeeServices(user.userId, employeeId, request)
        )
    }

    /**
     * Quita un servicio específico de un empleado.
     */
    @DeleteMapping("/{employeeId}/services/{serviceId}")
    fun removeServiceFromEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @PathVariable serviceId: Long
    ): ResponseEntity<Void> {
        employeeService.removeServiceFromEmployee(user.userId, employeeId, serviceId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Lista los horarios semanales configurados para un empleado.
     */
    @GetMapping("/{employeeId}/weekly-schedules")
    fun listWeeklySchedules(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<WeeklyScheduleResponse>> {
        return ResponseEntity.ok(employeeService.listWeeklySchedules(user.userId, employeeId))
    }

    /**
     * Crea o actualiza el horario semanal de un empleado para un día específico.
     */
    @PostMapping("/{employeeId}/weekly-schedules")
    fun upsertWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: WeeklyScheduleRequest
    ): ResponseEntity<WeeklyScheduleResponse> {
        return ResponseEntity.ok(employeeService.upsertWeeklySchedule(user.userId, employeeId, request))
    }

    /**
     * Elimina un horario semanal específico de un empleado.
     */
    @DeleteMapping("/{employeeId}/weekly-schedules/{weeklyScheduleId}")
    fun deleteWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @PathVariable weeklyScheduleId: Long
    ): ResponseEntity<Void> {
        employeeService.deleteWeeklySchedule(user.userId, employeeId, weeklyScheduleId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Lista los bloques de tiempo bloqueados (no disponibles) de un empleado.
     */
    @GetMapping("/{employeeId}/schedule-blocks")
    fun listScheduleBlocks(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<ScheduleBlockResponse>> {
        return ResponseEntity.ok(employeeService.listScheduleBlocks(user.userId, employeeId))
    }

    /**
     * Crea un bloque de tiempo en el que el empleado no estará disponible.
     */
    @PostMapping("/{employeeId}/schedule-blocks")
    fun createScheduleBlock(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: ScheduleBlockRequest
    ): ResponseEntity<ScheduleBlockResponse> {
        return ResponseEntity.ok(employeeService.createScheduleBlock(user.userId, employeeId, request))
    }

    /**
     * Elimina un bloque de disponibilidad de un empleado.
     */
    @DeleteMapping("/{employeeId}/schedule-blocks/{scheduleBlockId}")
    fun deleteScheduleBlock(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @PathVariable scheduleBlockId: Long
    ): ResponseEntity<Void> {
        employeeService.deleteScheduleBlock(user.userId, employeeId, scheduleBlockId)
        return ResponseEntity.noContent().build()
    }
}
