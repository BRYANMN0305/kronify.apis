package co.com.kronifyapis.service

import co.com.kronifyapis.dto.services.ServiceRequest
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Service as ServiceEntity
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.ServiceRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessRepository,
) {

    @Transactional
    fun createService(ownerId: UUID, businessId: UUID, request: ServiceRequest): ServiceResponse {
        val business = findOwnedBusiness(ownerId, businessId)

        val existingService = serviceRepository.findByBusiness_BusinessIdAndName(businessId, request.name)
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

    @Transactional
    fun listServices(ownerId: UUID, businessId: UUID): List<ServiceResponse> {
        findOwnedBusiness(ownerId, businessId)
        return serviceRepository.findAllByBusinessBusinessId(businessId)
            .map { it.toResponse() }
    }

    @Transactional
    fun getService(ownerId: UUID, businessId: UUID, serviceId: UUID): ServiceResponse {
        findOwnedBusiness(ownerId, businessId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")
        return service.toResponse()
    }

    @Transactional
    fun updateService(ownerId: UUID, businessId: UUID, serviceId: UUID, request: ServiceRequest): ServiceResponse {
        findOwnedBusiness(ownerId, businessId)
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

    @Transactional
    fun deleteService(ownerId: UUID, businessId: UUID, serviceId: UUID) {
        findOwnedBusiness(ownerId, businessId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")
        serviceRepository.delete(service)
    }

    private fun findOwnedBusiness(ownerId: UUID, businessId: UUID) =
        businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Business not found") }
            .also { business ->
                if (business.owner?.userId != ownerId) {
                    throw ForbiddenOperationException("Solo el dueño puede administrar los servicios")
                }
            }

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
