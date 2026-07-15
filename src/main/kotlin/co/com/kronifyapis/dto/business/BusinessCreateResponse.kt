package co.com.kronifyapis.dto.business

import java.time.LocalDateTime
import java.util.UUID

data class BusinessCreateResponse(
    val businessId: UUID,
    val name: String,
    val slug: String,
    val category: String?,
    val createdAt: LocalDateTime
)