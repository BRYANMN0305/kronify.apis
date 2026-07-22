package co.com.kronifyapis.dto.review

import java.time.LocalDateTime

data class ReviewResponse(
    val reviewId: Long,
    val appointmentId: Long,
    val businessId: Long,
    val customerId: Long,
    val customerName: String?,
    val rating: Int,
    val comment: String?,
    val visible: Boolean,
    val createdAt: LocalDateTime
)
