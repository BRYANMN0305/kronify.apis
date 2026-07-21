package co.com.kronifyapis.service

import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService
import co.com.kronifyapis.model.ScheduleBlock
import co.com.kronifyapis.model.Service
import co.com.kronifyapis.model.User
import co.com.kronifyapis.model.WeeklySchedule
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.EmployeeServiceRepository
import co.com.kronifyapis.repository.ScheduleBlockRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.WeeklyScheduleRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AvailabilityServiceTest {

    private val businessRepository = mockk<BusinessRepository>()
    private val serviceRepository = mockk<ServiceRepository>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val employeeServiceRepository = mockk<EmployeeServiceRepository>()
    private val weeklyScheduleRepository = mockk<WeeklyScheduleRepository>()
    private val scheduleBlockRepository = mockk<ScheduleBlockRepository>()
    private val appointmentRepository = mockk<AppointmentRepository>()

    private val availabilityService = AvailabilityService(
        businessRepository,
        serviceRepository,
        employeeRepository,
        employeeServiceRepository,
        weeklyScheduleRepository,
        scheduleBlockRepository,
        appointmentRepository
    )

    private val businessId = 1L
    private val serviceId = 10L
    private val employeeId = 100L
    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)

    private fun business(active: Boolean = true) = Business(
        businessId = businessId,
        slug = "mi-negocio",
        name = "Mi Negocio",
        active = active
    )

    private fun service(active: Boolean = true, durationMinutes: Int = 30) = Service(
        serviceId = serviceId,
        name = "Corte",
        durationMinutes = durationMinutes,
        active = active
    )

    private fun employee(active: Boolean = true) = Employee(
        employeeId = employeeId,
        user = User(userId = 5L, name = "Ana", lastName = "Pérez"),
        active = active
    )

    private fun weeklySchedule(startTime: LocalTime, endTime: LocalTime) = WeeklySchedule(
        weeklyScheduleId = 1L,
        dayOfWeek = tomorrow.dayOfWeek.value,
        startTime = startTime,
        endTime = endTime
    )

    private fun mockCommonLookups(
        businessActive: Boolean = true,
        serviceActive: Boolean = true,
        durationMinutes: Int = 30
    ) {
        every { businessRepository.findByBusinessId(businessId) } returns business(businessActive)
        every {
            serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
        } returns service(serviceActive, durationMinutes)
    }

    @Test
    fun `lanza excepcion si el negocio no existe`() {
        every { businessRepository.findByBusinessId(businessId) } returns null

        assertFailsWith<ResourceNotFoundException> {
            availabilityService.getAvailability(businessId, serviceId, tomorrow, null)
        }
    }

    @Test
    fun `lanza excepcion si el negocio esta inactivo`() {
        every { businessRepository.findByBusinessId(businessId) } returns business(active = false)

        assertFailsWith<ResourceNotFoundException> {
            availabilityService.getAvailability(businessId, serviceId, tomorrow, null)
        }
    }

    @Test
    fun `lanza excepcion si el servicio no pertenece al negocio o no existe`() {
        every { businessRepository.findByBusinessId(businessId) } returns business()
        every {
            serviceRepository.findByServiceIdAndBusinessBusinessId(serviceId, businessId)
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            availabilityService.getAvailability(businessId, serviceId, tomorrow, null)
        }
    }

    @Test
    fun `lanza excepcion si la fecha es en el pasado`() {
        mockCommonLookups()
        val ayer = LocalDate.now().minusDays(1)

        assertFailsWith<BadRequestException> {
            availabilityService.getAvailability(businessId, serviceId, ayer, null)
        }
    }

    @Test
    fun `lanza excepcion si el empleado indicado no pertenece al negocio`() {
        mockCommonLookups()
        every {
            employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)
        }
    }

    @Test
    fun `lanza excepcion si el empleado no tiene asignado el servicio`() {
        mockCommonLookups()
        every {
            employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId)
        } returns employee()
        every {
            employeeServiceRepository.existsByEmployeeAndService(any(), any())
        } returns false

        assertFailsWith<BadRequestException> {
            availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)
        }
    }

    @Test
    fun `no genera slots si el empleado no tiene horario ese dia`() {
        mockCommonLookups()
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        every { weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, tomorrow.dayOfWeek.value) } returns null

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)

        assertTrue(result.slots.isEmpty())
    }

    @Test
    fun `genera slots correctamente dentro de la ventana horaria sin bloqueos ni citas`() {
        mockCommonLookups(durationMinutes = 30)
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, tomorrow.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(9, 0), LocalTime.of(12, 0))
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)

        // Ventana de 09:00 a 12:00 (180 min), servicio de 30 min, paso de 15 min:
        // slots posibles: 09:00, 09:15, ..., 11:30 -> (180-30)/15 + 1 = 11
        assertEquals(11, result.slots.size)
        assertEquals(LocalTime.of(9, 0), result.slots.first().startAt.toLocalTime())
        assertEquals(LocalTime.of(11, 30), result.slots.last().startAt.toLocalTime())
        assertEquals(LocalTime.of(12, 0), result.slots.last().endAt.toLocalTime())
    }

    @Test
    fun `excluye slots que se solapan con una cita existente`() {
        mockCommonLookups(durationMinutes = 30)
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, tomorrow.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(9, 0), LocalTime.of(12, 0))
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()

        val citaExistente = Appointment(
            startAt = LocalDateTime.of(tomorrow, LocalTime.of(10, 0)),
            endAt = LocalDateTime.of(tomorrow, LocalTime.of(10, 30)),
            status = AppointmentStatus.CONFIRMED
        )
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns listOf(citaExistente)

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)

        val horasOcupadas = listOf(LocalTime.of(9, 45), LocalTime.of(10, 0), LocalTime.of(10, 15))
        horasOcupadas.forEach { hora ->
            assertTrue(
                result.slots.none { it.startAt.toLocalTime() == hora },
                "No debería existir un slot a las $hora"
            )
        }
        // El slot justo antes (09:30-10:00) y justo después (10:30-11:00) sí deben seguir disponibles
        assertTrue(result.slots.any { it.startAt.toLocalTime() == LocalTime.of(9, 30) })
        assertTrue(result.slots.any { it.startAt.toLocalTime() == LocalTime.of(10, 30) })
    }

    @Test
    fun `una cita cancelada no bloquea slots`() {
        mockCommonLookups(durationMinutes = 30)
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, tomorrow.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(9, 0), LocalTime.of(12, 0))
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()

        val citaCancelada = Appointment(
            startAt = LocalDateTime.of(tomorrow, LocalTime.of(10, 0)),
            endAt = LocalDateTime.of(tomorrow, LocalTime.of(10, 30)),
            status = AppointmentStatus.CANCELLED
        )
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns listOf(citaCancelada)

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)

        assertEquals(11, result.slots.size)
        assertTrue(result.slots.any { it.startAt.toLocalTime() == LocalTime.of(10, 0) })
    }

    @Test
    fun `excluye slots que se solapan con un bloqueo de horario`() {
        mockCommonLookups(durationMinutes = 30)
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, tomorrow.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(9, 0), LocalTime.of(12, 0))
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()

        val bloqueoAlmuerzo = ScheduleBlock(
            startAt = LocalDateTime.of(tomorrow, LocalTime.of(11, 0)),
            endAt = LocalDateTime.of(tomorrow, LocalTime.of(12, 0)),
            reason = "Almuerzo"
        )
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns listOf(bloqueoAlmuerzo)

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, employeeId)

        assertTrue(result.slots.none { it.startAt.toLocalTime() >= LocalTime.of(11, 0) })
        assertTrue(result.slots.any { it.startAt.toLocalTime() == LocalTime.of(10, 30) })
    }

    @Test
    fun `sin employeeId consulta todos los empleados activos que tengan el servicio asignado`() {
        mockCommonLookups(durationMinutes = 30)
        val empConServicio = employee(active = true)
        val empSinServicio = Employee(employeeId = 200L, user = User(userId = 6L, name = "Luis", lastName = "Gomez"), active = true)
        val empInactivo = Employee(employeeId = 300L, user = User(userId = 7L, name = "Sara", lastName = "Diaz"), active = false)

        every { employeeRepository.findAllByBusiness_BusinessId(businessId) } returns listOf(
            empConServicio, empSinServicio, empInactivo
        )
        every { employeeServiceRepository.existsByEmployeeAndService(empConServicio, any()) } returns true
        every { employeeServiceRepository.existsByEmployeeAndService(empSinServicio, any()) } returns false
        every { employeeServiceRepository.existsByEmployeeAndService(empInactivo, any()) } returns true

        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(empConServicio, tomorrow.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(9, 0), LocalTime.of(10, 0))
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(empConServicio, any(), any())
        } returns emptyList()
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(empConServicio.employeeId!!, any(), any())
        } returns emptyList()

        val result = availabilityService.getAvailability(businessId, serviceId, tomorrow, null)

        assertTrue(result.slots.isNotEmpty())
        assertTrue(result.slots.all { it.employeeId == empConServicio.employeeId })
    }

    @Test
    fun `no genera slots que ya hayan pasado si la fecha es hoy`() {
        mockCommonLookups(durationMinutes = 30)
        val hoy = LocalDate.now()
        val emp = employee()
        every { employeeRepository.findByEmployeeIdAndBusiness_BusinessId(employeeId, businessId) } returns emp
        every { employeeServiceRepository.existsByEmployeeAndService(any(), any()) } returns true
        // Horario amplio para cubrir todo el día
        every {
            weeklyScheduleRepository.findByEmployeeAndDayOfWeek(emp, hoy.dayOfWeek.value)
        } returns weeklySchedule(LocalTime.of(0, 0), LocalTime.of(23, 45))
        every {
            scheduleBlockRepository.findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()
        every {
            appointmentRepository.findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(any(), any(), any())
        } returns emptyList()

        val result = availabilityService.getAvailability(businessId, serviceId, hoy, employeeId)

        val ahora = LocalDateTime.now()
        assertTrue(result.slots.all { !it.startAt.isBefore(ahora) })
    }
}
