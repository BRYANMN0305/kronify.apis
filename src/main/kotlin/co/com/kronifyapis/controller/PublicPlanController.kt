package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.plan.PlanResponse
import co.com.kronifyapis.service.PlanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador público para consultar los planes de suscripción disponibles.
 */
@RestController
@RequestMapping("/public/plans")
class PublicPlanController(
    private val planService: PlanService
) {

    /**
     * Obtiene el catálogo público de planes disponibles para adquirir.
     */
    @GetMapping
    fun getPublicPlans(): ResponseEntity<List<PlanResponse>> {
        return ResponseEntity.ok(planService.getAllPlans())
    }
}
