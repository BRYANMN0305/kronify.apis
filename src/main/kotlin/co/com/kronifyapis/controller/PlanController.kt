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

@RestController
@RequestMapping("/business")
class PlanController(
    private val planService: PlanService
) {

    @GetMapping("/plan")
    fun getCurrentPlan(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<BusinessPlanUsageResponse> {
        return ResponseEntity.ok(planService.getCurrentPlan(user.userId))
    }

    @PostMapping("/plan")
    fun assignPlan(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: AssignPlanRequest
    ): ResponseEntity<BusinessPlanResponse> {
        return ResponseEntity.ok(planService.assignPlan(user.userId, request))
    }
}
