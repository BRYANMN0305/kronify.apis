package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.services.ServiceRequest
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.service.ServiceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/businesses/{businessId}/services")
class ServiceController(
    private val serviceService: ServiceService
) {

    @PostMapping("/")
    fun createService(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @Valid @RequestBody request: ServiceRequest
    ): ResponseEntity<ServiceResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(serviceService.createService(user.userId, businessId, request))
    }

    @GetMapping("/")
    fun listServices(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID
    ): ResponseEntity<List<ServiceResponse>> {
        return ResponseEntity.ok(serviceService.listServices(user.userId, businessId))
    }

    @GetMapping("/{serviceId}")
    fun getService(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<ServiceResponse> {
        return ResponseEntity.ok(serviceService.getService(user.userId, businessId, serviceId))
    }

    @PatchMapping("/{serviceId}")
    fun updateService(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID,
        @Valid @RequestBody request: ServiceRequest
    ): ResponseEntity<ServiceResponse> {
        return ResponseEntity.ok(serviceService.updateService(user.userId, businessId, serviceId, request))
    }

    @DeleteMapping("/{serviceId}")
    fun deleteService(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable businessId: UUID,
        @PathVariable serviceId: UUID
    ): ResponseEntity<ServiceResponse> {
        serviceService.deleteService(user.userId, businessId, serviceId)
        return ResponseEntity.noContent().build()
    }
}
