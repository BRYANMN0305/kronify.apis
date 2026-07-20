package co.com.kronifyapis.dto.services

import java.time.LocalDateTime
import java.util.UUID

data class ServiceResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val price: Double?,
    val durationMinutes: Int,
    val active: Boolean,
    val createdAt: LocalDateTime
)
