package co.com.kronifyapis.dto.activationcode

import java.time.LocalDateTime

data class ActivationCodeResponse(
    val activationCodeId: Long,
    val code: String,
    val planId: Long,
    val planName: String,
    val used: Boolean,
    val usedAt: LocalDateTime?,
    val usedByBusinessId: Long?,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime?
)
