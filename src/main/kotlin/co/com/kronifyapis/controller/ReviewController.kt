package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.review.ReviewCreateRequest
import co.com.kronifyapis.dto.review.ReviewResponse
import co.com.kronifyapis.dto.review.ReviewVisibilityRequest
import co.com.kronifyapis.service.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class ReviewController(
    private val reviewService: ReviewService
) {

    @PostMapping("/reviews")
    fun createReview(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: ReviewCreateRequest
    ): ResponseEntity<ReviewResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reviewService.createReview(user.userId, request))
    }

    @GetMapping("/business/reviews")
    fun listBusinessReviews(
        @AuthenticationPrincipal user: AuthenticatedUser,
        pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>> {
        return ResponseEntity.ok(reviewService.listBusinessReviews(user.userId, pageable))
    }

    @PatchMapping("/business/reviews/{reviewId}/visibility")
    fun updateVisibility(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @PathVariable reviewId: Long,
        @Valid @RequestBody request: ReviewVisibilityRequest
    ): ResponseEntity<ReviewResponse> {
        return ResponseEntity.ok(reviewService.updateReviewVisibility(user.userId, reviewId, request))
    }
}
