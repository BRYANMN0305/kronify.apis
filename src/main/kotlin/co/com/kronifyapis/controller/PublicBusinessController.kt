package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.availability.DayAvailabilityResponse
import co.com.kronifyapis.dto.publicpage.PublicBusinessResponse
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.service.AvailabilityService
import co.com.kronifyapis.service.PublicBusinessService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Controlador público para consultar información de negocios.
 */
@RestController
@RequestMapping("/public/businesses")
class PublicBusinessController(
    private val publicBusinessService: PublicBusinessService,
    private val availabilityService: AvailabilityService,
    private val businessRepository: BusinessRepository
) {

    /**
     * Obtiene la información pública de un negocio a partir de su slug.
     */
    @GetMapping("/{slug}")
    fun getBusinessBySlug(@PathVariable slug: String): ResponseEntity<PublicBusinessResponse> {
        return ResponseEntity.ok(publicBusinessService.getPublicBusinessBySlug(slug))
    }

    /**
     * Obtiene la disponibilidad de un negocio para un servicio y fecha específicos.
     * Acepta tanto el ID numérico como el slug del negocio.
     * Si se pasa employeeId, filtra por ese empleado.
     */
    @GetMapping("/{slugOrId}/availability")
    fun getAvailability(
        @PathVariable slugOrId: String,
        @RequestParam serviceId: Long,
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam date: LocalDate
    ): ResponseEntity<DayAvailabilityResponse> {
        // Intenta interpretar el slugOrId como ID numérico primero
        val businessId = slugOrId.toLongOrNull()
        if (businessId != null) {
            return ResponseEntity.ok(
                availabilityService.getAvailability(businessId, serviceId, date, employeeId)
            )
        }
        // Si no es número, lo busca por slug
        val business = businessRepository.findBusinessBySlug(slugOrId)
            ?: throw ResourceNotFoundException("Negocio no encontrado")
        return ResponseEntity.ok(
            availabilityService.getAvailability(business.businessId!!, serviceId, date, employeeId)
        )
    }
}
