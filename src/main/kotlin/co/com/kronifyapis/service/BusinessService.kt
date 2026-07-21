package co.com.kronifyapis.service

import co.com.kronifyapis.dto.business.BusinessCreateRequest
import co.com.kronifyapis.dto.business.BusinessCreateResponse
import co.com.kronifyapis.dto.business.BusinessSettingsResponse
import co.com.kronifyapis.dto.business.BusinessUpdateRequest
import co.com.kronifyapis.dto.business.BusinessUpdateResponse
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository,
    private val planService: PlanService,
    private val profileValidationHelper: ProfileValidationHelper
) {

    @Transactional
    fun createBusiness(ownerId: Long, request: BusinessCreateRequest): BusinessCreateResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw ResourceNotFoundException("Dueño no encontrado")

        profileValidationHelper.requireProfileType(ownerUser, ProfileType.BUSINESS)

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

        planService.assignFreePlanOnCreate(savedBusiness.businessId!!)

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
            createdAt = createdAt
        )
    }

    @Transactional(readOnly = true)
    fun getBusinessSettings(ownerId: Long): BusinessSettingsResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val business = businessRepository.findByOwner(ownerUser)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        return business.toSettingsResponse()
    }

    @Transactional
    fun updateBusiness(ownerId: Long, request: BusinessUpdateRequest): BusinessUpdateResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val business = businessRepository.findByOwner(ownerUser)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        request.name?.let { business.name = it.trim() }
        request.category?.let { business.category = it.trim() }
        request.description?.let { business.description = it.trim() }
        request.address?.let { business.address = it.trim() }
        request.logoUrl?.let { business.logoUrl = it.trim() }
        request.email?.let { business.email = it.trim().lowercase() }
        request.phoneNumber?.let { business.phoneNumber = it.trim() }
        request.whatsApp?.let { business.whatsapp = it.trim() }

        return businessRepository.save(business).toUpdateResponse()
    }

    private fun Business.toUpdateResponse(): BusinessUpdateResponse {
        return BusinessUpdateResponse(
            updatedAt = updatedAt
        )
    }

    private fun Business.toSettingsResponse(): BusinessSettingsResponse {
        return BusinessSettingsResponse(
            name = name,
            category = category,
            description = description,
            address = address,
            logoUrl = logoUrl,
            email = email,
            phoneNumber = phoneNumber,
            whatsApp = whatsapp,
            active = active,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

}
