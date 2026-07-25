package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.activationcode.ActivationCodeResponse
import co.com.kronifyapis.dto.activationcode.CreateActivationCodeRequest
import co.com.kronifyapis.service.PlanService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/activation-codes")
@SecurityRequirement(name = "apiKey")
class AdminActivationCodeController(
    private val planService: PlanService
) {

    @GetMapping
    fun getAllCodes(
        @RequestParam(required = false) used: Boolean?
    ): ResponseEntity<List<ActivationCodeResponse>> {
        return ResponseEntity.ok(planService.getActivationCodes(used))
    }

    @PostMapping
    fun createCode(
        @Valid @RequestBody request: CreateActivationCodeRequest
    ): ResponseEntity<ActivationCodeResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createActivationCode(request))
    }

    @DeleteMapping("/{id}")
    fun deleteCode(@PathVariable id: Long): ResponseEntity<Void> {
        planService.deleteActivationCode(id)
        return ResponseEntity.noContent().build()
    }
}
