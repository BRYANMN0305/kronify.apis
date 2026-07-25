package co.com.kronifyapis.dto.activationcode

import jakarta.validation.constraints.NotNull

data class CreateActivationCodeRequest(
    @field:NotNull
    val planId: Long,
    val code: String? = null,
    val expiresAt: String? = null
)
