package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.business.BusinessCreateRequest
import co.com.kronifyapis.dto.business.BusinessCreateResponse
import co.com.kronifyapis.dto.business.BusinessSettingsResponse
import co.com.kronifyapis.dto.business.BusinessUpdateRequest
import co.com.kronifyapis.dto.business.BusinessUpdateResponse
import co.com.kronifyapis.service.BusinessService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador para gestionar los datos del negocio.
 */
@RestController
@RequestMapping("/business")
class BusinessController(private val businessService: BusinessService) {

    /**
     * Crea un nuevo negocio asociado al usuario autenticado.
     */
    @PostMapping("/")
    fun createBusiness(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid
        @RequestBody request: BusinessCreateRequest
    ): ResponseEntity<BusinessCreateResponse> {
        val createdBusiness = businessService.createBusiness(user.userId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdBusiness)
    }

    /**
     * Actualiza los datos del negocio del usuario autenticado.
     */
    @PatchMapping("/")
    fun updateBusiness(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid
        @RequestBody request: BusinessUpdateRequest
    ): ResponseEntity<BusinessUpdateResponse> {
        val updatedBusiness = businessService.updateBusiness(user.userId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedBusiness)
    }

    /**
     * Devuelve la configuración actual del negocio del usuario autenticado.
     */
    @GetMapping("/me")
    fun getMyBusinessSettings(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<BusinessSettingsResponse> {
        return ResponseEntity.ok(businessService.getBusinessSettings(user.userId))
    }
}
