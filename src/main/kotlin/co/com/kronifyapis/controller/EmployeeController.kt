package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
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
}
