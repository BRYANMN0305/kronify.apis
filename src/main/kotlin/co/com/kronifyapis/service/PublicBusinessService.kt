package co.com.kronifyapis.service

import co.com.kronifyapis.dto.publicpage.PublicBusinessResponse
import co.com.kronifyapis.dto.publicpage.PublicEmployeeResponse
import co.com.kronifyapis.dto.publicpage.PublicServiceResponse
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ServiceRepository
import org.springframework.stereotype.Service as SpringService

/**
 * Servicio publico que cualquiera puede consultar sin autenticacion.
 * Devuelve la informacion de un negocio
 * para mostrarla en la pagina publica del negocio.
 */

@SpringService
class PublicBusinessService(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository
) {

    /**
     * Busca un negocio por su slug y devuelve
     * toda la info publica
     * con los IDs de servicios que ofrece cada uno.
     */
    fun getPublicBusinessBySlug(slug: String): PublicBusinessResponse {
        val business = businessRepository.findBusinessBySlug(slug)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        val businessId = business.businessId!!

        val services = serviceRepository.findAllByBusinessBusinessId(businessId)
            .filter { it.active }
            .map {
                PublicServiceResponse(
                    serviceId = it.serviceId!!,
                    name = it.name,
                    description = it.description,
                    durationMinutes = it.durationMinutes,
                    price = it.price
                )
            }

        val employees = employeeRepository.findAllByBusiness_BusinessId(businessId)
            .filter { it.active }
            .map { employee ->
                val serviceIds = employeeServiceRepository.findAllByEmployee(employee)
                    .mapNotNull { it.service?.serviceId }

                PublicEmployeeResponse(
                    employeeId = employee.employeeId!!,
                    name = "${employee.user?.name ?: ""} ${employee.user?.lastName ?: ""}".trim(),
                    serviceIds = serviceIds
                )
            }

        return PublicBusinessResponse(
            businessId = businessId,
            name = business.name,
            slug = business.slug,
            category = business.category,
            description = business.description,
            address = business.address,
            logoUrl = business.logoUrl,
            phoneNumber = business.phoneNumber,
            whatsapp = business.whatsapp,
            services = services,
            employees = employees
        )
    }
}
