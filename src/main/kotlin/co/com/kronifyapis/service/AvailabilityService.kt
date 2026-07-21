package co.com.kronifyapis.service

import co.com.kronifyapis.dto.appointment.AppointmentStatus
import co.com.kronifyapis.dto.availability.AvailabilitySlotResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AvailabilityService(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val appointmentRepository: AppointmentRepository
) {

    @Transactional(readOnly = true)
    fun getAvailableSlots(
        slug: String,
        serviceId: Long,
        employeeId: Long?,
        date: LocalDate
    ): List<AvailabilitySlotResponse> {
        val business = businessRepository.findBusinessBySlug(slug)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")
        val businessId = requireNotNull(business.businessId)

        val service = serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Servicio no encontrado")

        val employees = if (employeeId != null) {
            listOf(
                employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
                    ?.takeIf { it.active }
                    ?: throw ResourceNotFoundException("Empleado no encontrado")
            )
        } else {
            employeeRepository.findAllByBusiness_BusinessId(businessId).filter { it.active }
        }

        return employees
            .filter { employeeServiceRepository.existsByEmployeeAndService(it, service) }
            .flatMap { employee ->
                calculateSlots(employee, serviceId, service.durationMinutes, date)
            }
            .sortedBy { it.startAt }
    }

    @Transactional(readOnly = true)
    fun isSlotAvailable(employee: Employee, serviceDurationMinutes: Int, startAt: LocalDateTime): Boolean {
        val endAt = startAt.plusMinutes(serviceDurationMinutes.toLong())
        return isWithinWeeklySchedule(employee, startAt, endAt) && hasNoConflicts(employee, startAt, endAt)
    }

    private fun calculateSlots(
        employee: Employee,
        serviceId: Long,
        durationMinutes: Int,
        date: LocalDate
    ): List<AvailabilitySlotResponse> {
        if (durationMinutes <= 0) {
            throw BadRequestException("La duracion del servicio debe ser mayor a cero")
        }

        val schedule = weeklyScheduleRepository.findAllByEmployee(employee)
            .firstOrNull { it.dayOfWeek == date.dayOfWeek.value }
            ?: return emptyList()

        val dayStart = date.atTime(schedule.startTime)
        val dayEnd = date.atTime(schedule.endTime)
        val slots = mutableListOf<AvailabilitySlotResponse>()

        var candidateStart = dayStart
        while (!candidateStart.plusMinutes(durationMinutes.toLong()).isAfter(dayEnd)) {
            val candidateEnd = candidateStart.plusMinutes(durationMinutes.toLong())
            if (hasNoConflicts(employee, candidateStart, candidateEnd)) {
                slots += AvailabilitySlotResponse(
                    employeeId = requireNotNull(employee.employeeId),
                    serviceId = serviceId,
                    startAt = candidateStart,
                    endAt = candidateEnd
                )
            }
            candidateStart = candidateStart.plusMinutes(durationMinutes.toLong())
        }

        return slots
    }

    private fun isWithinWeeklySchedule(employee: Employee, startAt: LocalDateTime, endAt: LocalDateTime): Boolean {
        val schedule = weeklyScheduleRepository.findAllByEmployee(employee)
            .firstOrNull { it.dayOfWeek == startAt.dayOfWeek.value }
            ?: return false

        return startAt.toLocalDate() == endAt.toLocalDate() &&
            !startAt.toLocalTime().isBefore(schedule.startTime) &&
            !endAt.toLocalTime().isAfter(schedule.endTime)
    }

    private fun hasNoConflicts(employee: Employee, startAt: LocalDateTime, endAt: LocalDateTime): Boolean {
        val employeeId = requireNotNull(employee.employeeId)
        val appointmentConflict = appointmentRepository
            .findAllByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(employeeId, endAt, startAt)
            .any { it.status != AppointmentStatus.CANCELLED && it.status != AppointmentStatus.NO_SHOW }

        if (appointmentConflict) return false

        return scheduleBlockRepository
            .findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(employee, endAt, startAt)
            .isEmpty()
    }
}
