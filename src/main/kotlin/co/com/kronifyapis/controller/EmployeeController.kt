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

@RestController
@RequestMapping("/business/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping("/")
    fun listEmployees(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<EmployeeResponse>> {
        return ResponseEntity.ok(employeeService.listEmployees(user.userId))
    }

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

    @PostMapping("/owner/toggle")
    fun toggleOwnerEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: OwnerEmployeeToggleRequest
    ): ResponseEntity<EmployeeResponse> {
        return ResponseEntity.ok(employeeService.toggleOwnerEmployee(user.userId, request))
    }

    @GetMapping("/{employeeId}/services")
    fun listEmployeeServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(employeeService.listEmployeeServices(user.userId, employeeId))
    }

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

    @DeleteMapping("/{employeeId}/services/{serviceId}")
    fun removeServiceFromEmployee(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @PathVariable serviceId: Long
    ): ResponseEntity<Void> {
        employeeService.removeServiceFromEmployee(user.userId, employeeId, serviceId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{employeeId}/weekly-schedules")
    fun listWeeklySchedules(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<WeeklyScheduleResponse>> {
        return ResponseEntity.ok(employeeService.listWeeklySchedules(user.userId, employeeId))
    }

    @PostMapping("/{employeeId}/weekly-schedules")
    fun upsertWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: WeeklyScheduleRequest
    ): ResponseEntity<WeeklyScheduleResponse> {
        return ResponseEntity.ok(employeeService.upsertWeeklySchedule(user.userId, employeeId, request))
    }

    @DeleteMapping("/{employeeId}/weekly-schedules/{weeklyScheduleId}")
    fun deleteWeeklySchedule(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @PathVariable weeklyScheduleId: Long
    ): ResponseEntity<Void> {
        employeeService.deleteWeeklySchedule(user.userId, employeeId, weeklyScheduleId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{employeeId}/schedule-blocks")
    fun listScheduleBlocks(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long
    ): ResponseEntity<List<ScheduleBlockResponse>> {
        return ResponseEntity.ok(employeeService.listScheduleBlocks(user.userId, employeeId))
    }

    @PostMapping("/{employeeId}/schedule-blocks")
    fun createScheduleBlock(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable employeeId: Long,
        @Valid @RequestBody request: ScheduleBlockRequest
    ): ResponseEntity<ScheduleBlockResponse> {
        return ResponseEntity.ok(employeeService.createScheduleBlock(user.userId, employeeId, request))
    }

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
