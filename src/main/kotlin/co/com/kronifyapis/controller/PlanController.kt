package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.plan.AssignPlanRequest
import co.com.kronifyapis.dto.plan.BusinessPlanResponse
import co.com.kronifyapis.dto.plan.BusinessPlanUsageResponse
import co.com.kronifyapis.service.PlanService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador para gestionar el plan de suscripción del negocio.
 */
@RestController
@RequestMapping("/business")
class PlanController(
    private val planService: PlanService
) {

    /**
     * Devuelve el plan actual del negocio junto con el uso de sus límites.
     */
    @GetMapping("/plan")
    fun getCurrentPlan(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<BusinessPlanUsageResponse> {
        return ResponseEntity.ok(planService.getCurrentPlan(user.userId))
    }

    /**
     * Asigna un plan de suscripción al negocio del usuario autenticado.
     */
    @PostMapping("/plan")
    fun assignPlan(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: AssignPlanRequest
    ): ResponseEntity<BusinessPlanResponse> {
        return ResponseEntity.ok(planService.assignPlan(user.userId, request))
    }
}
