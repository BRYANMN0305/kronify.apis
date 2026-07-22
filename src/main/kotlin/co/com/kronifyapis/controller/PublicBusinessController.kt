package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.availability.DayAvailabilityResponse
import co.com.kronifyapis.dto.publicpage.PublicBusinessResponse
import co.com.kronifyapis.dto.review.ReviewResponse
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.service.AvailabilityService
import co.com.kronifyapis.service.PublicBusinessService
import co.com.kronifyapis.service.ReviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/public/businesses")
class PublicBusinessController(
    private val publicBusinessService: PublicBusinessService,
    private val availabilityService: AvailabilityService,
    private val businessRepository: BusinessRepository,
    private val reviewService: ReviewService
) {

    @GetMapping("/{slug}")
    fun getBusinessBySlug(@PathVariable slug: String): ResponseEntity<PublicBusinessResponse> {
        return ResponseEntity.ok(publicBusinessService.getPublicBusinessBySlug(slug))
    }

    @GetMapping("/{slug}/availability")
    fun getAvailability(
        @PathVariable slug: String,
        @RequestParam serviceId: Long,
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam date: LocalDate
    ): ResponseEntity<DayAvailabilityResponse> {
        val business = businessRepository.findBusinessBySlug(slug)
            ?: throw ResourceNotFoundException("Negocio no encontrado")
        return ResponseEntity.ok(
            availabilityService.getAvailability(business.businessId!!, serviceId, date, employeeId)
        )
    }

    @GetMapping("/{slug}/reviews")
    fun getPublicReviews(@PathVariable slug: String): ResponseEntity<List<ReviewResponse>> {
        return ResponseEntity.ok(reviewService.listPublicReviewsByBusinessSlug(slug))
    }
}
