package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockResponse
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleResponse
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService as EmployeeServiceEntity
import co.com.kronifyapis.model.ScheduleBlock
import co.com.kronifyapis.model.WeeklySchedule
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
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
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository
) {

    @Transactional
    fun listEmployees(userId: UUID, businessId: UUID): List<EmployeeResponse> {
        ensureCanManageBusiness(userId, businessId)
        return employeeRepository.findAllByBusiness_BusinessId(businessId).map { it.toEmployeeResponse() }
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
        return employeeRepository.save(employee).toEmployeeResponse()
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
            return saved.toEmployeeResponse()
        }

        if (employee == null) {
            throw ResourceNotFoundException("El dueño no tiene un registro de empleado asociado")
        }

        employee.active = false
        employee.selfManagedSchedule = false
        return employeeRepository.save(employee).toEmployeeResponse()
    }

    @Transactional(readOnly = true)
    fun listEmployeeServices(userId: UUID, businessId: UUID, employeeId: UUID): List<ServiceResponse> {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        return employeeServiceRepository.findAllByEmployee(employee)
            .mapNotNull { it.service }
                    .map { it.toServiceResponse() }
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
            .map { it.toServiceResponse() }
    }

    @Transactional
    fun removeServiceFromEmployee(userId: UUID, businessId: UUID, employeeId: UUID, serviceId: UUID) {
        ensureCanManageBusiness(userId, businessId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val employeeService = employeeServiceRepository.findByEmployeeAndService(employee, service)
            ?: throw ResourceNotFoundException("El empleado no tiene este servicio asociado")

        employeeServiceRepository.delete(employeeService)
    }

    @Transactional(readOnly = true)
    fun listWeeklySchedules(userId: UUID, businessId: UUID, employeeId: UUID): List<WeeklyScheduleResponse> {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        return weeklyScheduleRepository.findAllByEmployee(employee).map { it.toResponse() }
    }

    @Transactional
    fun upsertWeeklySchedule(
        userId: UUID,
        businessId: UUID,
        employeeId: UUID,
        request: WeeklyScheduleRequest
    ): WeeklyScheduleResponse {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        validateSelfManagedOrOwner(userId, businessId, employee)

        if (request.startTime >= request.endTime) {
            throw BadRequestException("La hora de inicio debe ser menor que la de fin")
        }

        val existing = weeklyScheduleRepository
            .findAllByEmployee(employee)
            .firstOrNull { it.dayOfWeek == request.dayOfWeek }

        val saved = weeklyScheduleRepository.save(
            (existing ?: WeeklySchedule()).apply {
                this.employee = employee
                dayOfWeek = request.dayOfWeek
                startTime = request.startTime
                endTime = request.endTime
            }
        )

        return saved.toResponse()
    }

    @Transactional
    fun deleteWeeklySchedule(userId: UUID, businessId: UUID, employeeId: UUID, weeklyScheduleId: UUID) {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        validateSelfManagedOrOwner(userId, businessId, employee)

        val weeklySchedule = weeklyScheduleRepository.findByWeeklyScheduleIdAndEmployee(weeklyScheduleId, employee)
            ?: throw ResourceNotFoundException("Horario no encontrado")

        weeklyScheduleRepository.delete(weeklySchedule)
    }

    @Transactional(readOnly = true)
    fun listScheduleBlocks(userId: UUID, businessId: UUID, employeeId: UUID): List<ScheduleBlockResponse> {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        return scheduleBlockRepository.findAllByEmployee(employee).map { it.toResponse() }
    }

    @Transactional
    fun createScheduleBlock(
        userId: UUID,
        businessId: UUID,
        employeeId: UUID,
        request: ScheduleBlockRequest
    ): ScheduleBlockResponse {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        validateSelfManagedOrOwner(userId, businessId, employee)

        if (request.startAt >= request.endAt) {
            throw BadRequestException("La fecha de inicio debe ser menor que la de fin")
        }

        if (scheduleBlockRepository.existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(employee, request.endAt, request.startAt)) {
            throw BadRequestException("El bloqueo se cruza con otro bloqueo existente")
        }

        val saved = scheduleBlockRepository.save(
            ScheduleBlock(
                employee = employee,
                startAt = request.startAt,
                endAt = request.endAt,
                reason = request.reason
            )
        )
        return saved.toResponse()
    }

    @Transactional
    fun deleteScheduleBlock(userId: UUID, businessId: UUID, employeeId: UUID, scheduleBlockId: UUID) {
        val employee = ensureCanManageOrSelfManage(userId, businessId, employeeId)
        validateSelfManagedOrOwner(userId, businessId, employee)

        val block = scheduleBlockRepository.findByScheduleBlockIdAndEmployee(scheduleBlockId, employee)
            ?: throw ResourceNotFoundException("Bloqueo no encontrado")

        scheduleBlockRepository.delete(block)
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

    private fun ensureCanManageOrSelfManage(userId: UUID, businessId: UUID, employeeId: UUID): Employee {
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }
        val ownerBusiness = businessRepository.findByOwner(user)
        val isOwner = ownerBusiness?.businessId == business.businessId
        val isSelf = employee.user?.userId == userId && employee.selfManagedSchedule

        if (!isOwner && !isSelf) {
            throw ForbiddenOperationException("No tiene permiso para gestionar este empleado")
        }
        return employee
    }

    private fun validateSelfManagedOrOwner(userId: UUID, businessId: UUID, employee: Employee) {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }
        val isOwner = business.owner?.userId == userId
        val isSelf = employee.user?.userId == userId && employee.selfManagedSchedule
        if (!isOwner && !isSelf) {
            throw ForbiddenOperationException("No tiene permiso para gestionar horarios o bloqueos")
        }
    }

    private fun Employee.toEmployeeResponse(): EmployeeResponse {
        val currentUser = requireNotNull(user)
        val currentBusiness = requireNotNull(business)
        return EmployeeResponse(
            employeeId = requireNotNull(employeeId),
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

    private fun co.com.kronifyapis.model.Service.toServiceResponse(): ServiceResponse {
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

    private fun WeeklySchedule.toResponse(): WeeklyScheduleResponse {
        return WeeklyScheduleResponse(
            weeklyScheduleId = requireNotNull(weeklyScheduleId),
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime
        )
    }

    private fun ScheduleBlock.toResponse(): ScheduleBlockResponse {
        return ScheduleBlockResponse(
            scheduleBlockId = requireNotNull(scheduleBlockId),
            startAt = startAt,
            endAt = endAt,
            reason = reason
        )
    }
}
