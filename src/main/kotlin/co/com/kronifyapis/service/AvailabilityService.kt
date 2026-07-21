package co.com.kronifyapis.service

import co.com.kronifyapis.dto.availability.DayAvailabilityResponse
import co.com.kronifyapis.dto.availability.TimeSlotResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Service
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import org.springframework.stereotype.Service as SpringService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringService
class AvailabilityService(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val appointmentRepository: AppointmentRepository
) {

    private val slotStepMinutes = 15L

    fun getAvailability(
        businessId: Long,
        serviceId: Long,
        date: LocalDate,
        employeeId: Long?
    ): DayAvailabilityResponse {
        val business = businessRepository.findByBusinessId(businessId)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, business.businessId!!)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Servicio no encontrado para este negocio")

        if (date.isBefore(LocalDate.now())) {
            throw BadRequestException("La fecha debe ser hoy o en el futuro")
        }

        val candidateEmployees = resolveCandidateEmployees(business.businessId!!, service, employeeId)

        val slots = candidateEmployees
            .flatMap { employee -> computeSlotsForEmployee(employee, service, date) }
            .sortedBy { it.startAt }

        return DayAvailabilityResponse(
            date = date,
            serviceId = service.serviceId!!,
            serviceDurationMinutes = service.durationMinutes,
            slots = slots
        )
    }

    private fun resolveCandidateEmployees(businessId: Long, service: Service, employeeId: Long?): List<Employee> {
        if (employeeId != null) {
            val employee = employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
                ?.takeIf { it.active }
                ?: throw ResourceNotFoundException("Empleado no encontrado para este negocio")

            if (!employeeServiceRepository.existsByEmployeeAndService(employee, service)) {
                throw BadRequestException("El empleado no tiene asignado este servicio")
            }
            return listOf(employee)
        }

        return employeeRepository.findAllByBusiness_BusinessId(businessId)
            .filter { it.active }
            .filter { employeeServiceRepository.existsByEmployeeAndService(it, service) }
    }

    private fun computeSlotsForEmployee(employee: Employee, service: Service, date: LocalDate): List<TimeSlotResponse> {
        val dayOfWeek = date.dayOfWeek.value
        val schedule = weeklyScheduleRepository.findByEmployeeAndDayOfWeek(employee, dayOfWeek)
            ?: return emptyList()

        val dayStart = LocalDateTime.of(date, LocalTime.MIDNIGHT)
        val dayEnd = dayStart.plusDays(1)

        val blocks = scheduleBlockRepository
            .findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(employee, dayEnd, dayStart)

        val busyAppointments = appointmentRepository
            .findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(employee.employeeId!!, dayEnd, dayStart)
            .filter { it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.NO_SHOW }

        val busyRanges = blocks.map { it.startAt to it.endAt } +
                busyAppointments.map { it.startAt to it.endAt }

        val employeeName = "${employee.user?.name ?: ""} ${employee.user?.lastName ?: ""}".trim()
        val durationMinutes = service.durationMinutes.toLong()
        val now = LocalDateTime.now()

        val slots = mutableListOf<TimeSlotResponse>()
        var candidateStart = LocalDateTime.of(date, schedule.startTime)
        val windowEnd = LocalDateTime.of(date, schedule.endTime)

        while (true) {
            val candidateEnd = candidateStart.plusMinutes(durationMinutes)
            if (candidateEnd.isAfter(windowEnd)) break

            val isPast = candidateStart.isBefore(now)
            val overlapsBusy = busyRanges.any { (busyStart, busyEnd) ->
                candidateStart.isBefore(busyEnd) && candidateEnd.isAfter(busyStart)
            }

            if (!isPast && !overlapsBusy) {
                slots.add(
                    TimeSlotResponse(
                        employeeId = employee.employeeId!!,
                        employeeName = employeeName,
                        startAt = candidateStart,
                        endAt = candidateEnd
                    )
                )
            }

            candidateStart = candidateStart.plusMinutes(slotStepMinutes)
        }

        return slots
    }
}
