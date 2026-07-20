package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService as EmployeeServiceEntity
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeServiceRepository: EmployeeServiceRepository
) {

    @Transactional(readOnly = true)
    fun listEmployees(userId: UUID, businessId: UUID): List<EmployeeResponse> {
        ensureCanManageBusiness(userId, businessId)
        return employeeRepository.findAllByBusiness_BusinessId(businessId).map { it.toResponse() }
    }

    @Transactional
    fun updateEmployeeSchedulePermission(
        userId: UUID,
        businessId: UUID,
        employeeId: UUID,
        request: EmployeeSchedulePermissionRequest
    ): EmployeeResponse {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        employee.selfManagedSchedule = request.selfManagedSchedule
        return employeeRepository.save(employee).toResponse()
    }

    @Transactional
    fun toggleOwnerEmployee(userId: UUID, businessId: UUID, request: OwnerEmployeeToggleRequest): EmployeeResponse {
        val business = ensureCanManageBusiness(userId, businessId)
        val ownerUser = business.owner ?: throw ResourceNotFoundException("Dueño no encontrado")
        val employee = employeeRepository.findByUserAndBusiness(ownerUser, business)

        if (request.enabled) {
            val saved = employeeRepository.save(
                (employee ?: Employee()).apply {
                    user = ownerUser
                    this.business = business
                    owner = true
                    active = true
                    selfManagedSchedule = true
                }
            )
            return saved.toResponse()
        }

        if (employee == null) {
            throw ResourceNotFoundException("El dueño no tiene un registro de empleado asociado")
        }

        employee.active = false
        employee.selfManagedSchedule = false
        return employeeRepository.save(employee).toResponse()
    }

    @Transactional(readOnly = true)
    fun listEmployeeServices(userId: UUID, businessId: UUID, employeeId: UUID): List<ServiceResponse> {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        return employeeServiceRepository.findAllByEmployee(employee)
            .mapNotNull { it.service }
            .map { it.toResponse() }
    }

    @Transactional
    fun updateEmployeeServices(
        userId: UUID,
        businessId: UUID,
        employeeId: UUID,
        request: EmployeeServiceUpdateRequest
    ): List<ServiceResponse> {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        val requestedServiceIds = request.serviceIds.distinct().toSet()
        val currentLinks = employeeServiceRepository.findAllByEmployee(employee)
        val currentServiceIds = currentLinks.mapNotNull { it.service?.serviceId }.toSet()

        val servicesToAdd = requestedServiceIds.minus(currentServiceIds).map { serviceId ->
            serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
                ?: throw ResourceNotFoundException("Servicio no encontrado")
        }

        val servicesToRemove = currentLinks.filter { link ->
            val serviceId = link.service?.serviceId
            serviceId == null || serviceId !in requestedServiceIds
        }

        if (servicesToRemove.isNotEmpty()) {
            employeeServiceRepository.deleteAll(servicesToRemove)
        }

        if (servicesToAdd.isNotEmpty()) {
            employeeServiceRepository.saveAll(
                servicesToAdd.map { service ->
                    EmployeeServiceEntity(
                        employee = employee,
                        service = service
                    )
                }
            )
        }

        return employeeServiceRepository.findAllByEmployee(employee)
            .mapNotNull { it.service }
            .map { it.toResponse() }
    }

    @Transactional
    fun removeServiceFromEmployee(userId: UUID, businessId: UUID, employeeId: UUID, serviceId: UUID) {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val employeeService = employeeServiceRepository.findByEmployeeAndService(employee, service)
            ?: throw ResourceNotFoundException("La relación entre empleado y servicio no existe")

        employeeServiceRepository.delete(employeeService)
    }

    private fun ensureCanManageBusiness(userId: UUID, businessId: UUID): co.com.kronifyapis.model.Business {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }
        val ownedBusiness = businessRepository.findByOwner(user)
            ?: throw ForbiddenOperationException("No tiene permiso para gestionar este negocio")

        if (ownedBusiness.businessId != business.businessId) {
            throw ForbiddenOperationException("No tiene permiso para gestionar este negocio")
        }

        return business
    }

    private fun Employee.toResponse(): EmployeeResponse {
        val currentUser = requireNotNull(user)
        val currentBusiness = requireNotNull(business)
        return EmployeeResponse(
            employeeId = requireNotNull(employeeId),
            userId = requireNotNull(currentUser.userId),
            businessId = requireNotNull(currentBusiness.businessId),
            name = currentUser.name,
            lastName = currentUser.lastName,
            email = currentUser.email,
            owner = owner,
            selfManagedSchedule = selfManagedSchedule,
            active = active,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
