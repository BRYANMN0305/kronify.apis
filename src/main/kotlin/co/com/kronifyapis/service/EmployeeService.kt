package co.com.kronifyapis.service

import co.com.kronifyapis.dto.employee.EmployeeResponse
import co.com.kronifyapis.dto.employee.EmployeeSchedulePermissionRequest
import co.com.kronifyapis.dto.employee.EmployeeServiceUpdateRequest
import co.com.kronifyapis.dto.employee.EmployeeUpdateRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockRequest
import co.com.kronifyapis.dto.employee.ScheduleBlockResponse
import co.com.kronifyapis.dto.employee.OwnerEmployeeToggleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleRequest
import co.com.kronifyapis.dto.employee.WeeklyScheduleResponse
import co.com.kronifyapis.dto.services.ServiceResponse
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService as EmployeeServiceEntity
import co.com.kronifyapis.model.ScheduleBlock
import co.com.kronifyapis.model.WeeklySchedule
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val appointmentRepository: AppointmentRepository
) {

    @Transactional
    fun listEmployees(userId: Long): List<EmployeeResponse> {
        val business = findOwnedBusiness(userId)
        return employeeRepository.findAllByBusiness_BusinessIdAndActiveTrue(business.businessId!!).map { it.toEmployeeResponse() }
    }

    @Transactional
    fun updateEmployeeSchedulePermission(
        userId: Long,
        employeeId: Long,
        request: EmployeeSchedulePermissionRequest
    ): EmployeeResponse {
        val business = findOwnedBusiness(userId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, business.businessId!!)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        employee.selfManagedSchedule = request.selfManagedSchedule
        return employeeRepository.save(employee).toEmployeeResponse()
    }

    @Transactional
    fun toggleOwnerEmployee(userId: Long, request: OwnerEmployeeToggleRequest): EmployeeResponse {
        val business = findOwnedBusiness(userId)
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
            throw BadRequestException("El dueño no tiene un registro de empleado asociado")
        }

        employee.owner = false
        employee.active = false
        employee.selfManagedSchedule = false
        return employeeRepository.save(employee).toEmployeeResponse()
    }

    @Transactional
    fun updateEmployee(
        userId: Long,
        employeeId: Long,
        request: EmployeeUpdateRequest
    ): EmployeeResponse {
        val business = findOwnedBusiness(userId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, business.businessId!!)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        if (request.active == false && employee.active) {
            val futureAppointments = appointmentRepository
                .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                    employee.employeeId!!, LocalDateTime.now().plusYears(1), LocalDateTime.now()
                )
                .filter { it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED }
            if (futureAppointments.isNotEmpty()) {
                throw BadRequestException(
                    "No se puede desactivar el empleado porque tiene ${futureAppointments.size} cita(s) futura(s). " +
                        "Reasigne o cancele las citas primero."
                )
            }
        }

        request.selfManagedSchedule?.let { employee.selfManagedSchedule = it }
        request.active?.let { employee.active = it }

        return employeeRepository.save(employee).toEmployeeResponse()
    }

    @Transactional
    fun deactivateEmployee(userId: Long, employeeId: Long): EmployeeResponse {
        val business = findOwnedBusiness(userId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, business.businessId!!)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        if (!employee.active) {
            throw BadRequestException("El empleado ya está inactivo")
        }

        val futureAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                employee.employeeId!!, LocalDateTime.now().plusYears(1), LocalDateTime.now()
            )
            .filter { it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED }

        if (futureAppointments.isNotEmpty()) {
            throw BadRequestException(
                "No se puede desactivar el empleado porque tiene ${futureAppointments.size} cita(s) futura(s). " +
                    "Reasigne o cancele las citas primero."
            )
        }

        employee.active = false
        return employeeRepository.save(employee).toEmployeeResponse()
    }

    @Transactional
    fun deleteEmployee(userId: Long, employeeId: Long) {
        val business = findOwnedBusiness(userId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, business.businessId!!)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        if (employee.owner) {
            throw BadRequestException("No se puede eliminar al dueño como empleado")
        }

        val futureAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                employee.employeeId!!, LocalDateTime.now().plusYears(1), LocalDateTime.now()
            )
            .filter { it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED }
        if (futureAppointments.isNotEmpty()) {
            throw BadRequestException(
                "No se puede eliminar el empleado porque tiene citas futuras. " +
                    "Reasigne o cancele las citas primero."
            )
        }

        weeklyScheduleRepository.findAllByEmployee(employee).forEach { weeklyScheduleRepository.delete(it) }
        scheduleBlockRepository.findAllByEmployee(employee).forEach { scheduleBlockRepository.delete(it) }
        employeeServiceRepository.findAllByEmployee(employee).forEach { employeeServiceRepository.delete(it) }
        employeeRepository.delete(employee)
    }

    @Transactional(readOnly = true)
    fun listEmployeeServices(userId: Long, employeeId: Long): List<ServiceResponse> {
        val business = findOwnedBusiness(userId)
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, business.businessId!!)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        return employeeServiceRepository.findAllByEmployee(employee)
            .mapNotNull { it.service }
            .map { it.toServiceResponse() }
    }

    @Transactional
    fun updateEmployeeServices(
        userId: Long,
        employeeId: Long,
        request: EmployeeServiceUpdateRequest
    ): List<ServiceResponse> {
        val business = findOwnedBusiness(userId)
        val businessId = business.businessId!!
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")

        val requestedServiceIds = request.serviceIds.distinct().toSet()
        val currentLinks = employeeServiceRepository.findAllByEmployee(employee)
        val currentServiceIds = currentLinks.mapNotNull { it.service?.serviceId }.toSet()

        val servicesToAdd = requestedServiceIds.minus(currentServiceIds).map { serviceId ->
            serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
                ?: throw ResourceNotFoundException("Servicio no encontrado")
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
    fun removeServiceFromEmployee(userId: Long, employeeId: Long, serviceId: Long) {
        val business = findOwnedBusiness(userId)
        val businessId = business.businessId!!
        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
            ?: throw ResourceNotFoundException("Empleado no encontrado")
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val employeeService = employeeServiceRepository.findByEmployeeAndService(employee, service)
            ?: throw BadRequestException("El empleado no tiene este servicio asociado")

        employeeServiceRepository.delete(employeeService)
    }

    @Transactional(readOnly = true)
    fun listWeeklySchedules(userId: Long, employeeId: Long): List<WeeklyScheduleResponse> {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        return weeklyScheduleRepository.findAllByEmployee(employee).map { it.toResponse() }
    }

    @Transactional
    fun upsertWeeklySchedule(
        userId: Long,
        employeeId: Long,
        request: WeeklyScheduleRequest
    ): WeeklyScheduleResponse {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        validateSelfManagedOrOwner(userId, employee)

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
    fun deleteWeeklySchedule(userId: Long, employeeId: Long, weeklyScheduleId: Long) {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        validateSelfManagedOrOwner(userId, employee)

        val weeklySchedule = weeklyScheduleRepository.findByWeeklyScheduleIdAndEmployee(weeklyScheduleId, employee)
            ?: throw ResourceNotFoundException("Horario no encontrado")

        weeklyScheduleRepository.delete(weeklySchedule)
    }

    @Transactional(readOnly = true)
    fun listScheduleBlocks(userId: Long, employeeId: Long): List<ScheduleBlockResponse> {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        return scheduleBlockRepository.findAllByEmployee(employee).map { it.toResponse() }
    }

    @Transactional
    fun createScheduleBlock(
        userId: Long,
        employeeId: Long,
        request: ScheduleBlockRequest
    ): ScheduleBlockResponse {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        validateSelfManagedOrOwner(userId, employee)

        if (request.startAt >= request.endAt) {
            throw BadRequestException("La fecha de inicio debe ser menor que la de fin")
        }

        if (scheduleBlockRepository.existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(employee, request.endAt, request.startAt)) {
            throw BadRequestException("El bloqueo se cruza con otro bloqueo existente")
        }

        val conflictingAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                employee.employeeId!!, request.endAt, request.startAt
            )
            .filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING }
        if (conflictingAppointments.isNotEmpty()) {
            throw BadRequestException(
                "No se puede crear el bloqueo porque existen citas ${if (conflictingAppointments.any { it.status == AppointmentStatus.CONFIRMED }) "confirmadas" else "pendientes"} en este horario. " +
                    "Debe cancelar o reprogramar las citas primero."
            )
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
    fun deleteScheduleBlock(userId: Long, employeeId: Long, scheduleBlockId: Long) {
        val employee = ensureCanManageOrSelfManage(userId, employeeId)
        validateSelfManagedOrOwner(userId, employee)

        val block = scheduleBlockRepository.findByScheduleBlockIdAndEmployee(scheduleBlockId, employee)
            ?: throw ResourceNotFoundException("Bloqueo no encontrado")

        scheduleBlockRepository.delete(block)
    }

    private fun findOwnedBusiness(userId: Long): Business {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return businessRepository.findByOwner(user)
            ?: throw ForbiddenOperationException("No tiene permiso para gestionar este negocio")
    }

    private fun ensureCanManageOrSelfManage(userId: Long, employeeId: Long): Employee {
        val employee = employeeRepository.findById(employeeId)
            .orElseThrow { ResourceNotFoundException("Empleado no encontrado") }
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val ownBusiness = businessRepository.findByOwner(user)
        val isOwner = ownBusiness?.businessId == employee.business?.businessId
        val isSelf = employee.user?.userId == userId && employee.selfManagedSchedule

        if (!isOwner && !isSelf) {
            throw ForbiddenOperationException("No tiene permiso para gestionar este empleado")
        }
        return employee
    }

    private fun validateSelfManagedOrOwner(userId: Long, employee: Employee) {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val ownBusiness = businessRepository.findByOwner(user)
        val isOwner = ownBusiness?.businessId == employee.business?.businessId
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
