package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
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
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping("/")
    fun listEmployees(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID
    ): ResponseEntity<List<EmployeeResponse>> {
        return ResponseEntity.ok(employeeService.listEmployees(user.userId, businessId))
    }

    @PatchMapping("/{employeeId}/schedule-permission")
    fun updateSchedulePermission(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @Valid @RequestBody request: EmployeeSchedulePermissionRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(
            employeeService.updateEmployeeSchedulePermission(user.userId, businessId, employeeId, request)
        )
    }

    @PostMapping("/owner/toggle")
    fun toggleOwnerEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: OwnerEmployeeToggleRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(employeeService.toggleOwnerEmployee(user.userId, businessId, request))
    }

    @GetMapping("/{employeeId}/services")
    fun listEmployeeServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(employeeService.listEmployeeServices(user.userId, businessId, employeeId))
    }

    @PatchMapping("/{employeeId}/services")
    fun updateEmployeeServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @Valid @RequestBody request: EmployeeServiceUpdateRequest
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(
            employeeService.updateEmployeeServices(user.userId, businessId, employeeId, request)
        )
    }

    @DeleteMapping("/{employeeId}/services/{serviceId}")
    fun removeServiceFromEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<Void> {
        employeeService.removeServiceFromEmployee(user.userId, businessId, employeeId, serviceId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{employeeId}/weekly-schedules")
    fun listWeeklySchedules(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID
    ): ResponseEntity<List<WeeklyScheduleResponse>> {
        return ResponseEntity.ok(employeeService.listWeeklySchedules(user.userId, businessId, employeeId))
    }

    @PostMapping("/{employeeId}/weekly-schedules")
    fun upsertWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @Valid @RequestBody request: WeeklyScheduleRequest
    ): ResponseEntity<WeeklyScheduleResponse> {
        return ResponseEntity.ok(employeeService.upsertWeeklySchedule(user.userId, businessId, employeeId, request))
    }

    @DeleteMapping("/{employeeId}/weekly-schedules/{weeklyScheduleId}")
    fun deleteWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @PathVariable weeklyScheduleId: UUID
    ): ResponseEntity<Void> {
        employeeService.deleteWeeklySchedule(user.userId, businessId, employeeId, weeklyScheduleId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{employeeId}/schedule-blocks")
    fun listScheduleBlocks(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID
    ): ResponseEntity<List<ScheduleBlockResponse>> {
        return ResponseEntity.ok(employeeService.listScheduleBlocks(user.userId, businessId, employeeId))
    }

    @PostMapping("/{employeeId}/schedule-blocks")
    fun createScheduleBlock(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @Valid @RequestBody request: ScheduleBlockRequest
    ): ResponseEntity<ScheduleBlockResponse> {
        return ResponseEntity.ok(employeeService.createScheduleBlock(user.userId, businessId, employeeId, request))
    }

    @DeleteMapping("/{employeeId}/schedule-blocks/{scheduleBlockId}")
    fun deleteScheduleBlock(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable employeeId: UUID,
        @PathVariable scheduleBlockId: UUID
    ): ResponseEntity<Void> {
        employeeService.deleteScheduleBlock(user.userId, businessId, employeeId, scheduleBlockId)
        return ResponseEntity.noContent().build()
    }
}
