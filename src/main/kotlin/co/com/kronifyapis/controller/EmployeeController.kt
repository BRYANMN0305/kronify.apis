package co.com.kronifyapis.controller

import co.com.kronifyapis.config.AuthenticatedUser
import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.service.EmployeeService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
}