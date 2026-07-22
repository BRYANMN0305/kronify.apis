package co.com.kronifyapis.service

import co.com.kronifyapis.dto.appointment.AppointmentAutofillResponse
import co.com.kronifyapis.dto.appointment.AppointmentCreateRequest
import co.com.kronifyapis.dto.appointment.AppointmentRescheduleRequest
import co.com.kronifyapis.dto.appointment.AppointmentResponse
import co.com.kronifyapis.dto.appointment.AppointmentStatusUpdateRequest
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Customer
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.enums.AppointmentOrigin
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.CustomerRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Servicio que maneja todo el ciclo de las citas.
 * Desde crear, listar, reprogramar hasta cancelar citas, ya sea que las haga
 * el negocio (privado) o un cliente desde la página pública.
 */
@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val planService: PlanService,
    private val profileValidationHelper: ProfileValidationHelper
) {
    /**
     * Revisa si se puede cambiar una cita de un estado a otro.
     * Por ejemplo, una cita PENDING puede pasar a CONFIRMED o CANCELLED,
     * pero una CANCELLED ya no se puede cambiar a nada mas.
     */
    private fun isAllowedTransition(current: AppointmentStatus, next: AppointmentStatus): Boolean {
        return when (current) {
            AppointmentStatus.PENDING -> next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.CANCELLED
            AppointmentStatus.CONFIRMED -> next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED || next == AppointmentStatus.NO_SHOW
            AppointmentStatus.CANCELLED -> false
            AppointmentStatus.COMPLETED -> false
            AppointmentStatus.NO_SHOW -> false
        }
    }

    /**
     * Obtiene los datos del usuario autenticado para auto-completar
     * el formulario de reserva. Sirve para no tener que escribir todo otra vez.
     */
    @Transactional(readOnly = true)
    fun getBookingAutofill(userId: Long): AppointmentAutofillResponse {
        val user = profileValidationHelper.requireClient(userId)
        val customer = customerRepository.findByUser_UserId(userId)

        return AppointmentAutofillResponse(
            userId = user.userId!!,
            customerId = customer?.customerId,
            name = customer?.name ?: user.name,
            lastName = customer?.lastName ?: user.lastName,
            phoneNumber = customer?.phoneNumber ?: user.phoneNumber,
            email = customer?.email ?: user.email
        )
    }

    /**
     * Crea una cita desde el panel del negocio (privado).
     * Valida que el empleado pertenezca al negocio y que el usuario tenga permiso.
     */
    @Transactional
    fun createAppointmentByBusiness(
        userId: Long,
        request: AppointmentCreateRequest
    ): AppointmentResponse {
        val business = findUserBusiness(userId)
        val businessId = business.businessId!!

        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(request.employeeId, businessId)
            ?: throw BadRequestException("El empleado no pertenece a este negocio")

        validateEmployeeActive(employee)
        ensureUserCanManageAppointments(userId, business, employee)

        return createAppointment(businessId, request, employee, AppointmentOrigin.PRIVATE)
    }

    /**
     * Crea una cita desde la pagina publica del negocio (cliente).
     * Si el usuario no ha iniciado sesion, se maneja como invitado.
     */
    @Transactional
    fun createAppointmentByClient(
        userId: Long?,
        businessId: Long,
        request: AppointmentCreateRequest
    ): AppointmentResponse {
        if (userId != null) {
            profileValidationHelper.requireClient(userId)
        }

        businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }
            .takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(request.employeeId, businessId)
            ?: throw BadRequestException("El empleado no pertenece a este negocio")

        validateEmployeeActive(employee)

        return createAppointment(businessId, request, employee, AppointmentOrigin.PUBLIC, userId)
    }

    /**
     * Metodo interno que hace la logica pesada de crear una cita:
     * valida el servicio, el horario, que no se cruce con otras citas,
     * que no haya bloqueos, y guarda todo en la base de datos.
     */
    private fun createAppointment(
        businessId: Long,
        request: AppointmentCreateRequest,
        employee: Employee,
        origin: AppointmentOrigin,
        clientUserId: Long? = null
    ): AppointmentResponse {
        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(request.serviceId, businessId)
            ?.takeIf { it.active }
            ?: throw BadRequestException("El servicio no pertenece a este negocio")

        if (!employeeServiceRepository.existsByEmployeeAndService(employee, service)) {
            throw BadRequestException("El empleado no tiene asignado este servicio")
        }

        planService.validateAppointmentLimit(businessId)

        val customer = resolveCustomer(request, origin, clientUserId)

        val startAt = request.startAt
        val endAt = startAt.plusMinutes(service.durationMinutes.toLong())

        if (startAt.isAfter(endAt) || startAt.isEqual(endAt)) {
            throw BadRequestException("La hora de inicio debe ser anterior a la hora de fin")
        }

        validateWithinWeeklySchedule(employee, startAt, endAt)

        val overlappingAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                employee.employeeId!!, endAt, startAt
            )
            .filter { it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.NO_SHOW }
        if (overlappingAppointments.isNotEmpty()) {
            throw ConflictException("El empleado ya tiene una cita en este horario")
        }

        val hasBlock = scheduleBlockRepository.existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
            employee, endAt, startAt
        )
        if (hasBlock) {
            throw ConflictException("El empleado tiene un bloqueo en este horario")
        }

        val appointment = Appointment(
            business = employee.business,
            service = service,
            employee = employee,
            customer = customer,
            startAt = startAt,
            endAt = endAt,
            status = AppointmentStatus.PENDING,
            origin = origin
        )

        val saved = appointmentRepository.save(appointment)
        return saved.toResponse(service.name, service.durationMinutes, employee, customer)
    }

    /**
     * Lista todas las citas de un negocio. Usa el userId para
     * encontrar el negocio asociado.
     */
    @Transactional(readOnly = true)
    fun listAppointments(userId: Long): List<AppointmentResponse> {
        val business = findUserBusiness(userId)
        val businessId = business.businessId!!

        return appointmentRepository.findAllByBusiness_BusinessId(businessId).map { appointment ->
            val service = appointment.service!!
            val employee = appointment.employee!!
            val customer = appointment.customer!!
            appointment.toResponse(service.name, service.durationMinutes, employee, customer)
        }
    }

    /**
     * Obtiene la agenda de uno o varios empleados en un rango de fechas.
     * Si no se pasa employeeId y el usuario es dueno, ve la agenda de todos.
     * Los empleados normales solo ven su propia agenda.
     */
    @Transactional(readOnly = true)
    fun getEmployeeAgenda(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        employeeId: Long?
    ): List<AppointmentResponse> {
        if (endDate.isBefore(startDate)) {
            throw BadRequestException("endDate debe ser igual o posterior a startDate")
        }

        val business = findUserBusiness(userId)
        val businessId = business.businessId!!
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val requestingEmployee = employeeRepository.findByUserAndBusiness(user, business)
        val canViewAll = business.owner?.userId == userId || requestingEmployee?.owner == true

        val targetEmployeeId = employeeId ?: if (canViewAll) null else requestingEmployee?.employeeId
            ?: throw ForbiddenOperationException("No tiene permiso para ver la agenda de este negocio")

        if (targetEmployeeId != null) {
            val targetEmployee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(targetEmployeeId, businessId)
                ?: throw ResourceNotFoundException("Empleado no encontrado")

            if (!canViewAll && targetEmployee.user?.userId != userId) {
                throw ForbiddenOperationException("No tiene permiso para ver la agenda de otro empleado")
            }
        }

        val startAt = startDate.atStartOfDay()
        val endAt = endDate.plusDays(1).atStartOfDay()
        val appointments = if (targetEmployeeId != null) {
            appointmentRepository
                .findAllByBusiness_BusinessIdAndEmployee_EmployeeIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                    businessId, targetEmployeeId, startAt, endAt
                )
        } else {
            appointmentRepository
                .findAllByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                    businessId, startAt, endAt
                )
        }

        return appointments.map { appointment ->
            val service = appointment.service!!
            val employee = appointment.employee!!
            val customer = appointment.customer!!
            appointment.toResponse(service.name, service.durationMinutes, employee, customer)
        }
    }

    /**
     * Obtiene los detalles de una cita especifica.
     * Verifica que la cita pertenezca al negocio del usuario.
     */
    @Transactional(readOnly = true)
    fun getAppointment(userId: Long, appointmentId: Long): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Cita no encontrada") }

        val business = findUserBusiness(userId)
        if (appointment.business?.businessId != business.businessId) {
            throw ForbiddenOperationException("No tiene permiso para ver esta cita")
        }

        val service = appointment.service!!
        val employee = appointment.employee!!
        val customer = appointment.customer!!
        return appointment.toResponse(service.name, service.durationMinutes, employee, customer)
    }

    /**
     * Cambia el estado de una cita (ej: de PENDING a CONFIRMED).
     * Solo permite transiciones validas (isAllowedTransition).
     * Si se cancela, guarda el motivo de cancelacion.
     */
    @Transactional
    fun updateAppointmentStatus(
        userId: Long,
        appointmentId: Long,
        request: AppointmentStatusUpdateRequest
    ): AppointmentResponse {
        val business = findUserBusiness(userId)
        val appointment = findAppointmentOrThrow(business.businessId!!, appointmentId)

        val employee = appointment.employee!!
        ensureUserCanManageAppointments(userId, business, employee)

        val currentStatus = appointment.status
        val newStatus = request.status

        if (!isAllowedTransition(currentStatus, newStatus)) {
            throw BadRequestException("No se permite cambiar de $currentStatus a $newStatus")
        }

        appointment.status = newStatus
        appointment.cancellationReason = request.cancellationReason

        val saved = appointmentRepository.save(appointment)
        val service = saved.service!!
        val emp = saved.employee!!
        val customer = saved.customer!!
        return saved.toResponse(service.name, service.durationMinutes, emp, customer)
    }

    /**
     * Reprograma una cita para una nueva fecha/hora.
     * Vuelve a validar horarios, cruces y bloqueos como si fuera una cita nueva.
     */
    @Transactional
    fun rescheduleAppointment(
        userId: Long,
        appointmentId: Long,
        request: AppointmentRescheduleRequest
    ): AppointmentResponse {
        val business = findUserBusiness(userId)

        val appointment = findAppointmentOrThrow(business.businessId!!, appointmentId)
        val employee = appointment.employee!!

        val canManage = isOwnerOrTargetEmployee(userId, business, employee)
        if (!canManage && !isClientOwner(userId, appointment)) {
            throw ForbiddenOperationException("No tiene permiso para reprogramar esta cita")
        }

        if (appointment.status == AppointmentStatus.CANCELLED || appointment.status == AppointmentStatus.COMPLETED) {
            throw BadRequestException("No se puede reprogramar una cita ${appointment.status}")
        }

        val service = appointment.service!!
        val newEndAt = request.startAt.plusMinutes(service.durationMinutes.toLong())

        validateWithinWeeklySchedule(employee, request.startAt, newEndAt)

        val overlappingAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
                employee.employeeId!!, newEndAt, request.startAt
            )
            .filter { it.appointmentId != appointmentId }
            .filter { it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.NO_SHOW }
        if (overlappingAppointments.isNotEmpty()) {
            throw ConflictException("El empleado ya tiene una cita en este horario")
        }

        val hasBlock = scheduleBlockRepository.existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
            employee, newEndAt, request.startAt
        )
        if (hasBlock) {
            throw ConflictException("El empleado tiene un bloqueo en este horario")
        }

        appointment.startAt = request.startAt
        appointment.endAt = newEndAt

        val saved = appointmentRepository.save(appointment)
        val customer = saved.customer!!
        return saved.toResponse(service.name, service.durationMinutes, employee, customer)
    }

    /**
     * Permite que un cliente cancele su propia cita desde la pagina publica.
     * Solo puede cancelar citas que le pertenezcan.
     */
    @Transactional
    fun cancelOwnAppointment(userId: Long, appointmentId: Long): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Cita no encontrada") }

        if (!isClientOwner(userId, appointment)) {
            throw ForbiddenOperationException("No puede cancelar una cita que no le pertenece")
        }

        if (appointment.status == AppointmentStatus.CANCELLED || appointment.status == AppointmentStatus.COMPLETED) {
            throw BadRequestException("La cita ya esta cancelada o completada")
        }

        appointment.status = AppointmentStatus.CANCELLED
        appointment.cancellationReason = "Cancelada por el cliente"

        val saved = appointmentRepository.save(appointment)
        val service = saved.service!!
        val employee = saved.employee!!
        val customer = saved.customer!!
        return saved.toResponse(service.name, service.durationMinutes, employee, customer)
    }

    /**
     * Decide quien es el cliente de la cita segun el origen:
     * - Si es publico y hay usuario logueado, busca o crea el Customer.
     * - Si es privado, usa el customerId que mandaron.
     * - Si es invitado, valida datos minimos y crea un Customer nuevo.
     */
    private fun resolveCustomer(
        request: AppointmentCreateRequest,
        origin: AppointmentOrigin,
        clientUserId: Long?
    ): Customer {
        if (origin == AppointmentOrigin.PUBLIC && request.customerId != null) {
            throw BadRequestException("customerId no se permite en reservas publicas")
        }

        if (origin == AppointmentOrigin.PUBLIC && clientUserId != null) {
            val user = profileValidationHelper.requireClient(clientUserId)
            val existing = customerRepository.findByUser_UserId(clientUserId)
            if (existing != null) {
                mergeRegisteredCustomer(existing, request)
                return customerRepository.save(existing)
            }

            return customerRepository.save(
                Customer(
                    user = user,
                    name = request.customerName?.trim()?.takeIf { it.isNotBlank() } ?: user.name,
                    lastName = request.customerLastName?.trim()?.takeIf { it.isNotBlank() } ?: user.lastName,
                    phoneNumber = request.customerPhone?.trim()?.takeIf { it.isNotBlank() } ?: user.phoneNumber,
                    email = request.customerEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: user.email
                )
            )
        }

        if (request.customerId != null) {
            return customerRepository.findById(request.customerId)
                .orElseThrow { BadRequestException("Cliente no encontrado") }
        }

        validateGuestCustomerData(request)

        request.customerEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { email ->
            val existingByEmail = customerRepository.findByEmail(email)
            if (existingByEmail.isNotEmpty()) {
                val existing = existingByEmail.first()
                existing.name = request.customerName!!.trim()
                existing.lastName = request.customerLastName?.trim()
                existing.phoneNumber = request.customerPhone!!.trim()
                existing.email = email
                return customerRepository.save(existing)
            }
        }

        val phone = request.customerPhone!!.trim()
        val existingByPhone = customerRepository.findByPhoneNumber(phone)
        if (existingByPhone.isNotEmpty()) {
            val existing = existingByPhone.first()
            existing.name = request.customerName!!.trim()
            existing.lastName = request.customerLastName?.trim()
            existing.email = request.customerEmail?.trim()
            return customerRepository.save(existing)
        }

        val customer = Customer(
            name = request.customerName!!.trim(),
            lastName = request.customerLastName?.trim(),
            phoneNumber = phone,
            email = request.customerEmail?.trim()?.lowercase()
        )
        return customerRepository.save(customer)
    }

    /**
     * Valida que un cliente invitado tenga nombre y telefono,
     * porque son obligatorios para crear la cita sin registro.
     */
    private fun validateGuestCustomerData(request: AppointmentCreateRequest) {
        if (request.customerName.isNullOrBlank()) {
            throw BadRequestException("customerName es requerido para reservas de invitados")
        }

        if (request.customerPhone.isNullOrBlank()) {
            throw BadRequestException("customerPhone es requerido para reservas de invitados")
        }
    }

    /**
     * Actualiza los datos de un cliente registrado si en la reserva
     * mandaron informacion nueva (nombre, telefono, etc.).
     */
    private fun mergeRegisteredCustomer(customer: Customer, request: AppointmentCreateRequest) {
        request.customerName?.trim()?.takeIf { it.isNotBlank() }?.let { customer.name = it }
        request.customerLastName?.trim()?.takeIf { it.isNotBlank() }?.let { customer.lastName = it }
        request.customerPhone?.trim()?.takeIf { it.isNotBlank() }?.let { customer.phoneNumber = it }
        request.customerEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { customer.email = it }
    }

    /**
     * Revisa que la cita caiga dentro del horario laboral del empleado
     * para ese dia de la semana. Si no hay horario configurado, truena.
     */
    private fun validateWithinWeeklySchedule(employee: Employee, startAt: LocalDateTime, endAt: LocalDateTime) {
        if (startAt.toLocalDate() != endAt.toLocalDate()) {
            throw BadRequestException("La cita esta fuera del horario laboral del empleado")
        }

        val weeklySchedule = weeklyScheduleRepository.findByEmployeeAndDayOfWeek(employee, startAt.dayOfWeek.value)
            ?: throw BadRequestException("El empleado no tiene horario configurado para este dia")

        val startTime = startAt.toLocalTime()
        val endTime = endAt.toLocalTime()
        if (startTime.isBefore(weeklySchedule.startTime) || endTime.isAfter(weeklySchedule.endTime)) {
            throw BadRequestException("La cita esta fuera del horario laboral del empleado")
        }
    }

    /**
     * Verifica que el empleado este activo antes de asignarle una cita.
     */
    private fun validateEmployeeActive(employee: Employee) {
        if (!employee.active) {
            throw BadRequestException("El empleado no esta activo")
        }
    }

    /**
     * Busca el negocio asociado a un usuario, ya sea como dueno o como empleado.
     */
    private fun findUserBusiness(userId: Long): Business {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        return businessRepository.findByOwner(user)
            ?: employeeRepository.findAllByUser_UserId(userId)
                .firstOrNull()
                ?.business
            ?: throw ResourceNotFoundException("No se encontro un negocio asociado al usuario")
    }

    /**
     * Busca una cita por ID y negocio, y si no existe lanza un error.
     */
    private fun findAppointmentOrThrow(businessId: Long, appointmentId: Long): Appointment {
        return appointmentRepository.findByAppointmentIdAndBusiness_BusinessId(appointmentId, businessId)
            ?: throw ResourceNotFoundException("Cita no encontrada")
    }

    /**
     * Verifica que el usuario tenga permiso para gestionar citas:
     * el dueno del negocio siempre puede, el empleado tambien si es el mismo,
     * y si es otro empleado, debe tener permisos de administrador (owner).
     */
    private fun ensureUserCanManageAppointments(userId: Long, business: Business, employee: Employee) {
        val isOwner = business.owner?.userId == userId
        val isTargetEmployee = employee.user?.userId == userId

        if (!isOwner && !isTargetEmployee) {
            val user = userRepository.findByUserId(userId)
                ?: throw ResourceNotFoundException("Usuario no encontrado")
            val managingEmployee = employeeRepository.findByUserAndBusiness(user, business)
            if (managingEmployee == null || !managingEmployee.owner) {
                throw ForbiddenOperationException("No tiene permiso para gestionar citas en este negocio")
            }
        }
    }

    /**
     * Revisa si el usuario es el dueno del negocio o el empleado de la cita.
     */
    private fun isOwnerOrTargetEmployee(userId: Long, business: Business, employee: Employee): Boolean {
        val isOwner = business.owner?.userId == userId
        val isTargetEmployee = employee.user?.userId == userId
        return isOwner || isTargetEmployee
    }

    /**
     * Revisa si el usuario es el dueno de la cita (el cliente que la reservo).
     */
    private fun isClientOwner(userId: Long, appointment: Appointment): Boolean {
        val customer = appointment.customer ?: return false
        return customer.user?.userId == userId
    }

    /**
     * Convierte el modelo Appointment a un AppointmentResponse
     * listo para devolver al frontend con todos los datos.
     */
    private fun Appointment.toResponse(
        serviceName: String,
        serviceDurationMinutes: Int,
        employee: Employee,
        customer: Customer
    ): AppointmentResponse {
        val employeeUser = employee.user
        return AppointmentResponse(
            appointmentId = requireNotNull(appointmentId),
            businessId = requireNotNull(business?.businessId),
            serviceId = requireNotNull(service?.serviceId),
            serviceName = serviceName,
            serviceDurationMinutes = serviceDurationMinutes,
            employeeId = requireNotNull(employee.employeeId),
            employeeName = "${employeeUser?.name ?: ""} ${employeeUser?.lastName ?: ""}".trim(),
            customerId = customer.customerId,
            customerName = customer.name ?: customer.user?.name,
            customerPhone = customer.phoneNumber ?: customer.user?.phoneNumber,
            customerEmail = customer.email ?: customer.user?.email,
            startAt = startAt,
            endAt = endAt,
            status = status,
            origin = origin,
            createdAt = createdAt
        )
    }
}
