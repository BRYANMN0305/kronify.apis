package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.availability.AvailabilitySlotResponse
import co.com.kronifyapis.dto.publicpage.PublicBusinessResponse
import co.com.kronifyapis.service.AvailabilityService
import co.com.kronifyapis.service.PublicBusinessService
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
    private val availabilityService: AvailabilityService
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
    ): ResponseEntity<List<AvailabilitySlotResponse>> {
        return ResponseEntity.ok(
            availabilityService.getAvailableSlots(slug, serviceId, employeeId, date)
        )
    }
}
