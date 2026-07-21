package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.publicpage.PublicBusinessResponse
import co.com.kronifyapis.service.PublicBusinessService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public/businesses")
class PublicBusinessController(
    private val publicBusinessService: PublicBusinessService
) {

    @GetMapping("/{slug}")
    fun getBusinessBySlug(@PathVariable slug: String): ResponseEntity<PublicBusinessResponse> {
        return ResponseEntity.ok(publicBusinessService.getPublicBusinessBySlug(slug))
    }
}