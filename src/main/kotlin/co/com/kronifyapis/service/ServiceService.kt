package co.com.kronifyapis.service

import co.com.kronifyapis.dto.services.ServiceRequest
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.model.Service as ServiceEntity
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Servicio para gestionar los servicios que ofrece un negocio.
 */
@Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val planService: PlanService,
    private val appointmentRepository: AppointmentRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
) {

    /**
     * Crea un nuevo servicio para el negocio.
     * Valida que no exista otro con el mismo nombre y que el plan permita crear mas.
     */
    @Transactional
    fun createService(userId: Long, request: ServiceRequest): ServiceResponse {
        val business = findOwnedBusiness(userId)

        planService.validateServiceLimit(business.businessId!!)

        val existingService = serviceRepository.findByBusiness_BusinessIdAndNameAndActiveTrue(business.businessId!!, request.name)
        if (existingService != null) {
            throw BadRequestException("El servicio ya existe")
        }

        val service = ServiceEntity(
            business = business,
            name = request.name.trim(),
            description = request.description,
            durationMinutes = request.durationMinutes,
            bufferMinutes = request.bufferMinutes,
            price = request.price
        )

        val createdService = serviceRepository.save(service)
        return createdService.toResponse()
    }

    /**
     * Lista todos los servicios activos del negocio del usuario.
     */
    @Transactional
    fun listServices(userId: Long): List<ServiceResponse> {
        val business = findOwnedBusiness(userId)
        return serviceRepository.findAllByBusinessBusinessIdAndActiveTrue(business.businessId!!)
            .map { it.toResponse() }
    }

    /**
     * Obtiene un servicio especifico por su ID.
     */
    @Transactional
    fun getService(userId: Long, serviceId: Long): ServiceResponse {
        val business = findOwnedBusiness(userId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessIdAndActiveTrue(serviceId, business.businessId!!)
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
        val service = serviceRepository.findByServiceIdAndBusinessBusinessIdAndActiveTrue(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val existingService = serviceRepository.findByBusiness_BusinessIdAndNameAndActiveTrue(businessId, request.name)
        if (existingService != null && existingService.serviceId != serviceId) {
            throw BadRequestException("El servicio ya existe")
        }

        service.name = request.name.trim()
        service.description = request.description
        service.durationMinutes = request.durationMinutes
        service.bufferMinutes = request.bufferMinutes
        service.price = request.price
        request.active?.let { service.active = it }

        return serviceRepository.save(service).toResponse()
    }

    /**
     * Elimina (soft delete) un servicio del negocio.
     * Verifica que no tenga citas futuras y desactiva sus asignaciones a empleados.
     */
    @Transactional
    fun deleteService(userId: Long, serviceId: Long) {
        val business = findOwnedBusiness(userId)
        val service = serviceRepository.findByServiceIdAndBusinessBusinessIdAndActiveTrue(serviceId, business.businessId!!)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val futureAppointments = appointmentRepository
            .findByServiceServiceIdAndStartAtGreaterThanEqualAndStatusIn(
                serviceId,
                LocalDateTime.now(),
                listOf(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            )
        if (futureAppointments.isNotEmpty()) {
            throw BadRequestException(
                "No se puede eliminar el servicio porque tiene ${futureAppointments.size} cita(s) futura(s). " +
                    "Reasigne o cancele las citas primero."
            )
        }

        employeeServiceRepository.findAllByServiceAndActiveTrue(service).forEach {
            it.active = false
            employeeServiceRepository.save(it)
        }

        service.active = false
        serviceRepository.save(service)
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
            bufferMinutes = bufferMinutes,
            active = active,
            createdAt = createdAt
        )
    }
}
