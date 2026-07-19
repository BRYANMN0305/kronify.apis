package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository
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
