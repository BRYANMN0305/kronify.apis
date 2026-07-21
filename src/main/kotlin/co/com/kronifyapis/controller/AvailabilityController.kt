package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.availability.DayAvailabilityResponse
import co.com.kronifyapis.service.AvailabilityService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/public/businesses/{businessId}/availability")
class AvailabilityController(
    private val availabilityService: AvailabilityService
) {

    @GetMapping
    fun getAvailability(
        @PathVariable businessId: Long,
        @RequestParam serviceId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(required = false) employeeId: Long?
    ): ResponseEntity<DayAvailabilityResponse> {
        return ResponseEntity.ok(
            availabilityService.getAvailability(businessId, serviceId, date, employeeId)
        )
    }
}