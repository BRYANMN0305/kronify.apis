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

@RestController
@RequestMapping("/business")
class BusinessController(private val businessService: BusinessService) {

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

    @PatchMapping("/")
    fun updateBusiness(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid
        @RequestBody request: BusinessCreateRequest
    ): ResponseEntity<BusinessUpdateResponse> {
        val updatedBusiness = businessService.updateBusiness(
            user.userId,
            BusinessUpdateRequest(
                name = request.name,
                category = request.category,
                description = request.description,
                address = request.address,
                logoUrl = request.logoUrl,
                email = request.email,
                phoneNumber = request.phoneNumber,
                whatsApp = request.whatsApp,
                ownerWorksAsEmployee = request.ownerWorksAsEmployee
            )
        )
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedBusiness)
    }

    @GetMapping("/me")
    fun getMyBusinessSettings(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<BusinessSettingsResponse> {
        return ResponseEntity.ok(businessService.getBusinessSettings(user.userId))
    }
}
