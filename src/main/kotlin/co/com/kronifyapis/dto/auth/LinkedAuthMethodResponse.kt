package co.com.kronifyapis.dto.auth

import java.time.LocalDateTime

data class LinkedAuthMethodResponse(
    val type: String,
    val provider: String?,
    val email: String,
    val linkedAt: LocalDateTime
)
