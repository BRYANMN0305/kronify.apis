package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.plan.CreatePlanRequest
import co.com.kronifyapis.dto.plan.PlanResponse
import co.com.kronifyapis.dto.plan.UpdatePlanRequest
import co.com.kronifyapis.service.PlanService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/plans")
@SecurityRequirement(name = "apiKey")
class AdminPlanController(
    private val planService: PlanService
) {

    @GetMapping
    fun getAllPlans(): ResponseEntity<List<PlanResponse>> {
        return ResponseEntity.ok(planService.getAllPlans())
    }

    @GetMapping("/{id}")
    fun getPlanById(@PathVariable id: Long): ResponseEntity<PlanResponse> {
        return ResponseEntity.ok(planService.getPlanById(id))
    }

    @PostMapping
    fun createPlan(
        @Valid @RequestBody request: CreatePlanRequest
    ): ResponseEntity<PlanResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request))
    }

    @PutMapping("/{id}")
    fun updatePlan(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePlanRequest
    ): ResponseEntity<PlanResponse> {
        return ResponseEntity.ok(planService.updatePlan(id, request))
    }

    @DeleteMapping("/{id}")
    fun deletePlan(@PathVariable id: Long): ResponseEntity<Void> {
        planService.deletePlan(id)
        return ResponseEntity.noContent().build()
    }
}
