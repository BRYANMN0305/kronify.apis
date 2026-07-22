package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {

    fun existsByAppointment_AppointmentId(appointmentId: Long): Boolean

    fun findAllByAppointment_Business_BusinessIdAndVisibleTrueOrderByCreatedAtDesc(businessId: Long): List<Review>

    fun findAllByAppointment_Business_BusinessIdOrderByCreatedAtDesc(businessId: Long): List<Review>

    fun findByReviewIdAndAppointment_Business_BusinessId(reviewId: Long, businessId: Long): Review?
}
