package co.com.kronifyapis.service

import co.com.kronifyapis.dto.availability.DayAvailabilityResponse
import co.com.kronifyapis.dto.availability.TimeSlotResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Service
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessOpeningHourRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import co.com.kronifyapis.service.AvailabilityCalculator
import co.com.kronifyapis.service.BusyInterval
import org.springframework.stereotype.Service as SpringService
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Servicio que consulta la disponibilidad de un negocio para un servicio y fecha especificos.
 * Revisa horarios laborales, citas existentes y bloqueos para decirte
 * que horarios estan libres.
 */
@SpringService
class AvailabilityService(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val businessOpeningHourRepository: BusinessOpeningHourRepository,
    private val appointmentRepository: AppointmentRepository
) {

    private val slotStepMinutes = 15L

    /**
     * Obtiene los horarios disponibles para un servicio en una fecha.
     * Si se pasa employeeId, solo muestra slots para ese empleado;
     * si no, muestra de todos los empleados que ofrecen el servicio.
     */
    fun getAvailability(
        businessId: Long,
        serviceId: Long,
        date: LocalDate,
        employeeId: Long?
    ): DayAvailabilityResponse {
        val business = businessRepository.findByBusinessId(businessId)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        val service = serviceRepository.findByServiceIdAndBusinessBusinessIdAndActiveTrue(serviceId, business.businessId!!)
            ?: throw ResourceNotFoundException("Servicio no encontrado para este negocio")

        if (date.isBefore(LocalDate.now())) {
            throw BadRequestException("La fecha debe ser hoy o en el futuro")
        }

        val candidateEmployees = resolveCandidateEmployees(business.businessId!!, service, employeeId)

        val slots = candidateEmployees
            .flatMap { employee -> computeSlotsForEmployee(business, employee, service, date) }
            .sortedBy { it.startAt }

        return DayAvailabilityResponse(
            date = date,
            serviceId = service.serviceId!!,
            serviceDurationMinutes = service.durationMinutes,
            slots = slots
        )
    }

    /**
     * Filtra los empleados que pueden realizar el servicio:
     * si se pidio un empleado especifico, solo ese; si no, todos los activos
     * que tengan el servicio asignado.
     */
    private fun resolveCandidateEmployees(businessId: Long, service: Service, employeeId: Long?): List<Employee> {
        if (employeeId != null) {
            val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessIdAndActiveTrue(employeeId, businessId)
                ?: throw ResourceNotFoundException("Empleado no encontrado para este negocio")

            if (!employeeServiceRepository.existsByEmployeeAndServiceAndActiveTrue(employee, service)) {
                throw BadRequestException("El empleado no tiene asignado este servicio")
            }
            return listOf(employee)
        }

        return employeeRepository.findAllByBusiness_BusinessIdAndActiveTrue(businessId)
            .filter { employeeServiceRepository.existsByEmployeeAndServiceAndActiveTrue(it, service) }
    }

    /**
     * Calcula los slots disponibles para un empleado en una fecha:
     * - Obtiene el horario de atención del negocio y el horario laboral del empleado de ese día
     * - La ventana real es la intersección (business hours ∩ employee hours)
     * - Busca bloqueos y citas ocupadas
     * - Usa AvailabilityCalculator para generar los espacios libres
     * - Filtra los slots que ya pasaron (no se puede agendar en pasado)
     */
    private fun computeSlotsForEmployee(
        business: Business,
        employee: Employee,
        service: Service,
        date: LocalDate
    ): List<TimeSlotResponse> {
        val dayOfWeek = date.dayOfWeek.value
        val schedule = weeklyScheduleRepository.findByEmployeeAndDayOfWeekAndActiveTrue(employee, dayOfWeek)
            ?: return emptyList()

        val opening = businessOpeningHourRepository.findByBusinessAndDayOfWeekAndActiveTrue(business, dayOfWeek)
            ?: return emptyList()

        val workingStart = maxOf(schedule.startTime, opening.startTime)
        val workingEnd = minOf(schedule.endTime, opening.endTime)
        if (!workingStart.isBefore(workingEnd)) return emptyList()

        val dayStart = LocalDateTime.of(date, LocalTime.MIDNIGHT)
        val dayEnd = dayStart.plusDays(1)

        val blocks = scheduleBlockRepository
            .findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThanAndActiveTrue(employee, dayEnd, dayStart)

        val busyAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(employee.employeeId!!, dayEnd, dayStart)
            .filter { it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.NO_SHOW }

        val busyIntervals = buildList {

            // Un bloque o cita puede cruzar la medianoche (ej: 23:00 - 01:00).
            // Se recorta el intervalo al día consultado y, si el tramo llega al
            // borde del día, se representa con LocalTime.MIN / LocalTime.MAX.
            blocks.forEach { block ->
                val from = if (block.startAt.isAfter(dayStart)) block.startAt else dayStart
                val to = if (block.endAt.isBefore(dayEnd)) block.endAt else dayEnd
                if (from.isBefore(to)) {
                    add(
                        BusyInterval(
                            start = if (from == dayStart) LocalTime.MIN else from.toLocalTime(),
                            end = if (to == dayEnd) LocalTime.MAX else to.toLocalTime()
                        )
                    )
                }
            }

            busyAppointments.forEach { appointment ->
                val bufferMinutes = appointment.service?.bufferMinutes ?: 0
                val rawStart = appointment.startAt
                val rawEnd = appointment.endAt.plusMinutes(bufferMinutes.toLong())
                val from = if (rawStart.isAfter(dayStart)) rawStart else dayStart
                val to = if (rawEnd.isBefore(dayEnd)) rawEnd else dayEnd
                if (from.isBefore(to)) {
                    add(
                        BusyInterval(
                            start = if (from == dayStart) LocalTime.MIN else from.toLocalTime(),
                            end = if (to == dayEnd) LocalTime.MAX else to.toLocalTime()
                        )
                    )
                }
            }
        }

        val employeeName = "${employee.user?.name ?: ""} ${employee.user?.lastName ?: ""}".trim()
        val now = LocalDateTime.now()

        return AvailabilityCalculator.calculateAvailableSlots(
            workingStart = workingStart,
            workingEnd = workingEnd,
            durationMinutes = service.durationMinutes + service.bufferMinutes,
            busyIntervals = busyIntervals,
            stepMinutes = slotStepMinutes.toInt()
        )
            .map { startTime ->
                val startAt = LocalDateTime.of(date, startTime)
                val endAt = startAt.plusMinutes(service.durationMinutes.toLong())
                TimeSlotResponse(
                    employeeId = employee.employeeId!!,
                    employeeName = employeeName,
                    startAt = startAt,
                    endAt = endAt
                )
            }
            .filter { !it.startAt.isBefore(now) }
    }
}
