package co.com.kronifyapis.service

import co.com.kronifyapis.dto.business.BusinessCreateRequest
import co.com.kronifyapis.dto.business.BusinessCreateResponse
import co.com.kronifyapis.dto.business.BusinessSettingsResponse
import co.com.kronifyapis.dto.business.BusinessUpdateRequest
import co.com.kronifyapis.dto.business.BusinessUpdateResponse
import co.com.kronifyapis.dto.business.OpeningHourRequest
import co.com.kronifyapis.dto.business.OpeningHourResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.BusinessOpeningHour
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.repository.BusinessOpeningHourRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.PlanRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


/**
 * Servicio para gestionar negocios
 */
@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val employeeRepository: EmployeeRepository,
    private val userRepository: UserRepository,
    private val planRepository: PlanRepository,
    private val businessOpeningHourRepository: BusinessOpeningHourRepository,
    private val planService: PlanService,
    private val profileValidationHelper: ProfileValidationHelper
) {

    /**
     * Crea un negocio nuevo. Valida que el slug sea unico,
     * que el usuario no tenga ya un negocio, y asigna el plan FREE.
     * Si el dueno quiere trabajar como empleado, lo crea automaticamente.
     */
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
        val businessId = savedBusiness.businessId!!

        val selectedPlanId = request.planId
        if (selectedPlanId != null) {
            val plan = planRepository.findById(selectedPlanId)
                .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

            if (plan.requiresActivationCode) {
                val code = request.activationCode
                    ?: throw BadRequestException("Este plan requiere un código de activación")
                planService.validateAndUseActivationCode(code, selectedPlanId, businessId)
            }

            planService.assignPlanToBusiness(businessId, plan)
        } else {
            planService.assignFreePlanOnCreate(businessId)
        }

        if (request.ownerWorksAsEmployee && !employeeRepository.existsByUserAndBusinessAndActiveTrue(ownerUser, savedBusiness)) {
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

        request.openingHours?.let { saveOpeningHours(savedBusiness, it) }

        return savedBusiness.toCreateResponse()
    }

    private fun Business.toCreateResponse(): BusinessCreateResponse {
        return BusinessCreateResponse(
            createdAt = createdAt
        )
    }

    /**
     * Obtiene la configuracion actual del negocio (nombre, direccion, etc.).
     */
    @Transactional(readOnly = true)
    fun getBusinessSettings(ownerId: Long): BusinessSettingsResponse {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val business = businessRepository.findByOwner(ownerUser)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        return business.toSettingsResponse(listOpeningHours(business))
    }

    /**
     * Actualiza los datos del negocio: nombre, categoria, descripcion,
     * direccion, logo, email, telefono y WhatsApp.
     */
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

    private fun Business.toSettingsResponse(openingHours: List<OpeningHourResponse>): BusinessSettingsResponse {
        return BusinessSettingsResponse(
            name = name,
            category = category,
            description = description,
            address = address,
            logoUrl = logoUrl,
            email = email,
            phoneNumber = phoneNumber,
            whatsApp = whatsapp,
            openingHours = openingHours,
            active = active,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Lista los horarios de atención semanales configurados para el negocio
     * del usuario autenticado (solo dueño).
     */
    @Transactional(readOnly = true)
    fun getOpeningHours(ownerId: Long): List<OpeningHourResponse> {
        return listOpeningHours(findOwnedBusiness(ownerId))
    }

    /**
     * Reemplaza por completo el horario de atención semanal del negocio
     * (solo dueño). Los horarios previos se desactivan (soft delete).
     */
    @Transactional
    fun updateOpeningHours(ownerId: Long, requests: List<OpeningHourRequest>): List<OpeningHourResponse> {
        val business = findOwnedBusiness(ownerId)
        saveOpeningHours(business, requests)
        return listOpeningHours(business)
    }

    /**
     * Desactiva los horarios activos actuales del negocio y guarda el nuevo set.
     * Valida que inicio < fin y que no haya días repetidos.
     */
    private fun saveOpeningHours(business: Business, requests: List<OpeningHourRequest>) {
        val seenDays = mutableSetOf<Int>()
        for (request in requests) {
            if (!seenDays.add(request.dayOfWeek)) {
                throw BadRequestException("El día ${request.dayOfWeek} está repetido en el horario de atención")
            }
            if (request.startTime >= request.endTime) {
                throw BadRequestException("La hora de inicio debe ser menor que la de fin")
            }
        }

        businessOpeningHourRepository.findAllByBusinessAndActiveTrue(business)
            .forEach { it.active = false }
        // Flush obliga a ejecutar los UPDATEs de desactivación ANTES de insertar las
        // nuevas filas: Hibernate ejecuta los INSERTs antes que los UPDATEs en un mismo
        // flush, y con la unique (business_id, day_of_week, active) eso rompería.
        businessOpeningHourRepository.flush()
        businessOpeningHourRepository.saveAll(
            requests.map { request ->
                BusinessOpeningHour().apply {
                    this.business = business
                    dayOfWeek = request.dayOfWeek
                    startTime = request.startTime
                    endTime = request.endTime
                }
            }
        )
    }

    private fun listOpeningHours(business: Business): List<OpeningHourResponse> {
        return businessOpeningHourRepository.findAllByBusinessAndActiveTrue(business)
            .sortedBy { it.dayOfWeek }
            .map { it.toResponse() }
    }

    private fun BusinessOpeningHour.toResponse(): OpeningHourResponse {
        return OpeningHourResponse(
            businessOpeningHourId = requireNotNull(businessOpeningHourId),
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime
        )
    }

    private fun findOwnedBusiness(ownerId: Long): Business {
        val ownerUser = userRepository.findByUserId(ownerId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return businessRepository.findByOwner(ownerUser)
            ?: throw ForbiddenOperationException("No tiene permiso para gestionar este negocio")
    }

}
