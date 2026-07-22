package co.com.kronifyapis.dto.review

import jakarta.validation.constraints.NotNull

data class ReviewVisibilityRequest(
    @field:NotNull
    val visible: Boolean
)
