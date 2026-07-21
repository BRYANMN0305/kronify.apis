package co.com.kronifyapis.service

import co.com.kronifyapis.dto.plan.AssignPlanRequest
import co.com.kronifyapis.dto.plan.BusinessPlanResponse
import co.com.kronifyapis.dto.plan.BusinessPlanUsageResponse
import co.com.kronifyapis.dto.plan.PlanResponse
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.BusinessPlan
import co.com.kronifyapis.model.Plan
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessPlanRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.PlanRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class PlanService(
    private val planRepository: PlanRepository,
    private val businessPlanRepository: BusinessPlanRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository,
    private val appointmentRepository: AppointmentRepository,
    private val employeeRepository: EmployeeRepository,
    private val profileValidationHelper: ProfileValidationHelper
) {

    @PostConstruct
    fun initPlans() {
        if (planRepository.count() == 0L) {
            planRepository.save(
                Plan().apply {
                    name = "FREE"
                    serviceLimit = 5
                    monthlyAppointmentLimit = 50
                    employeeLimit = 3
                }
            )
            planRepository.save(
                Plan().apply {
                    name = "PRO"
                    serviceLimit = null
                    monthlyAppointmentLimit = null
                    employeeLimit = null
                }
            )
        }
    }


    @Transactional
    fun assignFreePlanOnCreate(businessId: Long) {
        val freePlan = planRepository.findByName("FREE")
            ?: throw ResourceNotFoundException("Plan FREE no encontrado")

        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }

        businessPlanRepository.save(
            BusinessPlan().apply {
                this.business = business
                this.plan = freePlan
                this.active = true
                this.startAt = LocalDateTime.now()
            }
        )
    }

    @Transactional
    fun assignPlan(userId: Long, request: AssignPlanRequest): BusinessPlanResponse {
        val user = profileValidationHelper.requireBusiness(userId)

        val business = businessRepository.findByOwner(user)
            ?: throw ResourceNotFoundException("No tienes un negocio registrado. Solo el propietario del negocio puede asignar planes.")

        val plan = planRepository.findById(request.planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

        val currentPlan = businessPlanRepository.findByBusiness_BusinessIdAndActiveTrue(business.businessId!!)
        if (currentPlan != null) {
            currentPlan.active = false
            currentPlan.endAt = LocalDateTime.now()
            businessPlanRepository.save(currentPlan)
        }

        val newPlan = businessPlanRepository.save(
            BusinessPlan().apply {
                this.business = business
                this.plan = plan
                this.active = true
                this.startAt = LocalDateTime.now()
            }
        )

        return newPlan.toResponse()
    }

    @Transactional(readOnly = true)
    fun getCurrentPlan(userId: Long): BusinessPlanUsageResponse {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val business = businessRepository.findByOwner(user)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        return getBusinessPlanUsage(business.businessId!!)
    }

    @Transactional(readOnly = true)
    fun getBusinessPlanUsage(businessId: Long): BusinessPlanUsageResponse {
        val businessPlan = businessPlanRepository.findByBusiness_BusinessIdAndActiveTrue(businessId)
            ?: throw ResourceNotFoundException("Plan no asignado al negocio")

        val plan = businessPlan.plan!!

        val serviceCount = serviceRepository.countByBusiness_BusinessId(businessId)

        val now = LocalDateTime.now()
        val yearMonth = YearMonth.from(now)
        val monthStart = yearMonth.atDay(1).atStartOfDay()
        val monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay()
        val currentMonthAppointmentCount = appointmentRepository.countByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotIn(
            businessId, monthStart, monthEnd, listOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        )

        val employeeCount = employeeRepository.countByBusiness_BusinessId(businessId)

        val serviceLimit = plan.serviceLimit
        val monthlyLimit = plan.monthlyAppointmentLimit
        val empLimit = plan.employeeLimit

        val serviceLimitReached = serviceLimit != null && serviceCount >= serviceLimit
        val appointmentLimitReached = monthlyLimit != null && currentMonthAppointmentCount >= monthlyLimit
        val employeeLimitReached = empLimit != null && employeeCount >= empLimit

        return BusinessPlanUsageResponse(
            plan = plan.toResponse(),
            active = businessPlan.active,
            startAt = businessPlan.startAt,
            endAt = businessPlan.endAt,
            serviceCount = serviceCount,
            currentMonthAppointmentCount = currentMonthAppointmentCount,
            employeeCount = employeeCount,
            serviceLimitReached = serviceLimitReached,
            appointmentLimitReached = appointmentLimitReached,
            employeeLimitReached = employeeLimitReached
        )
    }

    fun validateServiceLimit(businessId: Long) {
        val businessPlan = businessPlanRepository.findActiveWithLock(businessId)
            ?: return

        val limit = businessPlan.plan!!.serviceLimit ?: return
        val currentCount = serviceRepository.countByBusiness_BusinessId(businessId)
        if (currentCount >= limit) {
            throw ForbiddenOperationException(
                "Límite de servicios alcanzado ($currentCount/$limit). Actualice su plan para crear más servicios."
            )
        }
    }

    fun validateAppointmentLimit(businessId: Long) {
        val businessPlan = businessPlanRepository.findActiveWithLock(businessId)
            ?: return

        val limit = businessPlan.plan!!.monthlyAppointmentLimit ?: return
        val now = LocalDateTime.now()
        val yearMonth = YearMonth.from(now)
        val monthStart = yearMonth.atDay(1).atStartOfDay()
        val monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay()
        val currentCount = appointmentRepository.countByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotIn(
            businessId, monthStart, monthEnd, listOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
        )
        if (currentCount >= limit) {
            throw ForbiddenOperationException(
                "Límite mensual de citas alcanzado ($currentCount/$limit). Actualice su plan para crear más citas."
            )
        }
    }

    fun validateEmployeeLimit(businessId: Long) {
        val businessPlan = businessPlanRepository.findActiveWithLock(businessId)
            ?: return

        val limit = businessPlan.plan!!.employeeLimit ?: return
        val currentCount = employeeRepository.countByBusiness_BusinessId(businessId)
        if (currentCount >= limit) {
            throw ForbiddenOperationException(
                "Límite de empleados alcanzado ($currentCount/$limit). Actualice su plan para agregar más empleados."
            )
        }
    }

    private fun Plan.toResponse(): PlanResponse {
        return PlanResponse(
            planId = requireNotNull(planId),
            name = name,
            serviceLimit = serviceLimit,
            monthlyAppointmentLimit = monthlyAppointmentLimit,
            employeeLimit = employeeLimit
        )
    }

    private fun BusinessPlan.toResponse(): BusinessPlanResponse {
        return BusinessPlanResponse(
            plan = requireNotNull(plan).toResponse(),
            active = active,
            startAt = startAt,
            endAt = endAt
        )
    }
}
