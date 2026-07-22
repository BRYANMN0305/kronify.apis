package co.com.kronifyapis.service

import co.com.kronifyapis.dto.services.ServiceRequest
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Service as ServiceEntity
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Servicio para gestionar los servicios que ofrece un negocio.
 */
@Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val planService: PlanService,
) {

    /**
     * Crea un nuevo servicio para el negocio.
     * Valida que no exista otro con el mismo nombre y que el plan permita crear mas.
     */
    @Transactional
    fun createService(userId: Long, request: ServiceRequest): ServiceResponse {
        val business = findOwnedBusiness(userId)

        planService.validateServiceLimit(business.businessId!!)

        val existingService = serviceRepository.findByBusiness_BusinessIdAndName(business.businessId!!, request.name)
        if (existingService != null) {
            throw BadRequestException("El servicio ya existe")
        }

        val service = ServiceEntity(
            business = business,
            name = request.name.trim(),
            description = request.description,
            durationMinutes = request.durationMinutes,
            price = request.price
        )

        val createdService = serviceRepository.save(service)
        return createdService.toResponse()
    }

    /**
     * Lista todos los servicios del negocio del usuario.
     */
    @Transactional
    fun listServices(userId: Long): List<ServiceResponse> {
        val business = findOwnedBusiness(userId)
        return serviceRepository.findAllByBusinessBusinessId(business.businessId!!)
            .map { it.toResponse() }
    }

    /**
     * Obtiene un servicio especifico por su ID.
     */
    @Transactional
    fun getService(userId: Long, serviceId: Long): ServiceResponse {
        val business = findOwnedBusiness(userId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, business.businessId!!)
            ?: throw ResourceNotFoundException("Servicio no encontrado")
        return service.toResponse()
    }

    /**
     * Actualiza los datos de un servicio existente.
     */
    @Transactional
    fun updateService(userId: Long, serviceId: Long, request: ServiceRequest): ServiceResponse {
        val business = findOwnedBusiness(userId)
        val businessId = business.businessId!!
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val existingService = serviceRepository.findByBusiness_BusinessIdAndName(businessId, request.name)
        if (existingService != null && existingService.serviceId != serviceId) {
            throw BadRequestException("El servicio ya existe")
        }

        service.name = request.name.trim()
        service.description = request.description
        service.durationMinutes = request.durationMinutes
        service.price = request.price

        return serviceRepository.save(service).toResponse()
    }

    /**
     * Elimina un servicio del negocio.
     */
    @Transactional
    fun deleteService(userId: Long, serviceId: Long) {
        val business = findOwnedBusiness(userId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, business.businessId!!)
            ?: throw ResourceNotFoundException("Servicio no encontrado")
        serviceRepository.delete(service)
    }

    /**
     * Busca el negocio del usuario. Solo el dueno puede administrar servicios.
     */
    private fun findOwnedBusiness(userId: Long): Business {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return businessRepository.findByOwner(user)
            ?: throw ForbiddenOperationException("Solo el dueño puede administrar los servicios")
    }

    /**
     * Convierte la entidad Service a ServiceResponse para devolver al frontend.
     */
    private fun ServiceEntity.toResponse(): ServiceResponse {
        return ServiceResponse(
            id = requireNotNull(serviceId),
            name = name,
            description = description,
            price = price,
            durationMinutes = durationMinutes,
            active = active,
            createdAt = createdAt
        )
    }
}
