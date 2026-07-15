package co.com.kronifyapis.dto.business

import org.aspectj.bridge.Message
import java.time.LocalDateTime
import java.util.UUID

data class BusinessUpdateResponse(

    val businessId: UUID,
    val message: String,
    val updatedAt: LocalDateTime
)
