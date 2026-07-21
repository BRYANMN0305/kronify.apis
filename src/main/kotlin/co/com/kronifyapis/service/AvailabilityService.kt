package co.com.kronifyapis.service

import co.com.kronifyapis.dto.appointment.AppointmentStatus
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
import java.time.LocalDate
import java.time.LocalDateTime

class AvailabilityService(
    private val businessRepository: BusinessRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val employeeServiceRepository: EmployeeServiceRepository,
    private val weeklyScheduleRepository: WeeklyScheduleRepository,
    private val scheduleBlockRepository: ScheduleBlockRepository,
    private val appointmentRepository: AppointmentRepository
) {

        serviceId: Long,
            ?.takeIf { it.active }
            ?: throw ResourceNotFoundException("Negocio no encontrado")

            ?.takeIf { it.active }

                    ?.takeIf { it.active }
        }

            .filter { employeeServiceRepository.existsByEmployeeAndService(it, service) }
    }

            ?: return emptyList()


                    startAt = candidateStart,
                    endAt = candidateEnd
                )
            }
        }

        return slots
    }
    }