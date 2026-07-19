package co.com.kronifyapis.service

import co.com.kronifyapis.dto.business.BusinessCreateRequest
import co.com.kronifyapis.dto.business.BusinessCreateResponse
import co.com.kronifyapis.dto.business.BusinessUpdateResponse
import co.com.kronifyapis.dto.user.ProfileType
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createBusiness(ownerId: UUID, request: BusinessCreateRequest): BusinessCreateResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw InvalidCredentialsException("Dueño no encontrado")

        if (ownerUser.profileType != ProfileType.BUSINESS) {
            throw ForbiddenOperationException("El perfil del usuario no es BUSINESS")
        }

        val normalizedSlug = request.slug.trim().lowercase()

        if (businessRepository.existsBusinessBySlug(normalizedSlug)) {
            throw ConflictException("El slug ya está en uso")
        }

        if (businessRepository.existsByOwner(ownerUser)) {
            throw ConflictException("El usuario ya tiene una empresa")
        }

        val business = Business().apply {
            this.owner = ownerUser
            this.name = request.name.trim()
            this.slug = normalizedSlug
            this.category = request.category?.trim()
            this.description = request.description?.trim()
            this.address = request.address?.trim()
            this.logoUrl = request.logoUrl?.trim()
            this.email = request.email?.trim()?.lowercase()
            this.phoneNumber = request.phoneNumber?.trim()
            this.whatsapp = request.whatsApp?.trim()
        }

        val savedBusiness = businessRepository.save(business)

        if (request.ownerWorksAsEmployee && !employeeRepository.existsByUserAndBusiness(ownerUser, savedBusiness)) {
            employeeRepository.save(
                Employee().apply {
                    user = ownerUser
                    this.business = savedBusiness
                    this.owner = true
                    selfManagedSchedule = true
                    active = true
                }
            )
        }

        return savedBusiness.toCreateResponse()
    }

    private fun Business.toCreateResponse(): BusinessCreateResponse {
        return BusinessCreateResponse(
            businessId = requireNotNull(businessId),
            name = name,
            slug = slug,
            category = category,
            createdAt = createdAt
        )
    }

    @Transactional
    fun updateBusiness(ownerId: UUID, request: BusinessCreateRequest): BusinessUpdateResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw InvalidCredentialsException("Usuario no encontrado")

        val business = businessRepository.findByOwner(ownerUser)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        request.name?.let { business.name = it }
        request.slug?.let { business.slug = it }
        request.category?.let { business.category = it }
        request.description?.let { business.description = it }
        request.address?.let { business.address = it }
        request.logoUrl?.let { business.logoUrl = it }
        request.email?.let { business.email = it }
        request.phoneNumber?.let { business.phoneNumber = it.trim() }
        request.whatsApp?.let { business.whatsapp = it.trim() }

        return businessRepository.save(business).toUpdateResponse()
    }

    private fun Business.toUpdateResponse(): BusinessUpdateResponse {
        return BusinessUpdateResponse(
            businessId = requireNotNull(businessId),
            message = "Negocio actualizado exitosamente",
            updatedAt = updatedAt
        )
    }

}
