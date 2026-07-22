package co.com.kronifyapis.service

import co.com.kronifyapis.dto.review.ReviewCreateRequest
import co.com.kronifyapis.dto.review.ReviewResponse
import co.com.kronifyapis.dto.review.ReviewVisibilityRequest
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Customer
import co.com.kronifyapis.model.Review
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.CustomerRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.ReviewRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val appointmentRepository: AppointmentRepository,
    private val customerRepository: CustomerRepository,
    private val businessRepository: BusinessRepository,
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository,
    private val profileValidationHelper: ProfileValidationHelper
) {

    @Transactional
    fun createReview(userId: Long, request: ReviewCreateRequest): ReviewResponse {
        // Solo un cliente registrado puede dejar resena; los invitados no tienen usuario para validar propiedad.
        profileValidationHelper.requireClient(userId)

        val customer = customerRepository.findByUser_UserId(userId)
            ?: throw ForbiddenOperationException("Solo clientes registrados con una cita pueden dejar reseñas")

        val appointment = appointmentRepository.findById(request.appointmentId)
            .orElseThrow { ResourceNotFoundException("Cita no encontrada") }

        if (appointment.customer?.customerId != customer.customerId) {
            throw ForbiddenOperationException("No puede reseñar una cita que no le pertenece")
        }

        // La resena queda amarrada a una cita completada para evitar opiniones sin servicio realizado.
        if (appointment.status != AppointmentStatus.COMPLETED) {
            throw BadRequestException("Solo se pueden reseñar citas completadas")
        }

        if (reviewRepository.existsByAppointment_AppointmentId(request.appointmentId)) {
            throw ConflictException("La cita ya tiene una reseña")
        }

        val review = reviewRepository.save(
            Review(
                appointment = appointment,
                customer = customer,
                rating = request.rating,
                comment = request.comment?.trim()?.takeIf { it.isNotBlank() },
                visible = true
            )
        )

        return review.toResponse()
    }

    @Transactional(readOnly = true)
    fun listPublicReviewsByBusinessSlug(slug: String): List<ReviewResponse> {
        val business = businessRepository.findBusinessBySlug(slug)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        // En la pagina publica solo muestro resenas visibles; el negocio puede ocultar alguna si hace falta.
        return reviewRepository
            .findAllByAppointment_Business_BusinessIdAndVisibleTrueOrderByCreatedAtDesc(business.businessId!!)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun listBusinessReviews(userId: Long): List<ReviewResponse> {
        val business = findUserBusiness(userId)
        return reviewRepository
            .findAllByAppointment_Business_BusinessIdOrderByCreatedAtDesc(business.businessId!!)
            .map { it.toResponse() }
    }

    @Transactional
    fun updateReviewVisibility(
        userId: Long,
        reviewId: Long,
        request: ReviewVisibilityRequest
    ): ReviewResponse {
        val business = findUserBusiness(userId)
        val review = reviewRepository.findByReviewIdAndAppointment_Business_BusinessId(reviewId, business.businessId!!)
            ?: throw ResourceNotFoundException("Reseña no encontrada")

        review.visible = request.visible
        return reviewRepository.save(review).toResponse()
    }

    private fun findUserBusiness(userId: Long): Business {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return businessRepository.findByOwner(user)
            ?: employeeRepository.findAllByUser_UserId(userId)
                .firstOrNull()
                ?.business
            ?: throw ResourceNotFoundException("No se encontro un negocio asociado al usuario")
    }

    private fun Review.toResponse(): ReviewResponse {
        val appointment = requireNotNull(appointment)
        val customer = requireNotNull(customer)
        return ReviewResponse(
            reviewId = requireNotNull(reviewId),
            appointmentId = requireNotNull(appointment.appointmentId),
            businessId = requireNotNull(appointment.business?.businessId),
            customerId = requireNotNull(customer.customerId),
            customerName = customer.displayName(),
            rating = rating,
            comment = comment,
            visible = visible,
            createdAt = createdAt
        )
    }

    private fun Customer.displayName(): String? {
        return listOfNotNull(name, lastName)
            .joinToString(" ")
            .trim()
            .takeIf { it.isNotBlank() }
            ?: user?.let { "${it.name} ${it.lastName}".trim() }
    }
}
