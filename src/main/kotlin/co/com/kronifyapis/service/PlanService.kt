package co.com.kronifyapis.service

import co.com.kronifyapis.dto.activationcode.ActivationCodeResponse
import co.com.kronifyapis.dto.activationcode.CreateActivationCodeRequest
import co.com.kronifyapis.dto.plan.AssignPlanRequest
import co.com.kronifyapis.dto.plan.BusinessPlanResponse
import co.com.kronifyapis.dto.plan.BusinessPlanUsageResponse
import co.com.kronifyapis.dto.plan.CreatePlanRequest
import co.com.kronifyapis.dto.plan.PlanResponse
import co.com.kronifyapis.dto.plan.UpdatePlanRequest
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.ForbiddenOperationException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.ActivationCode
import co.com.kronifyapis.model.BusinessPlan
import co.com.kronifyapis.model.Plan
import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.model.enums.SubscriptionStatus
import co.com.kronifyapis.repository.ActivationCodeRepository
import co.com.kronifyapis.repository.AppointmentRepository
import co.com.kronifyapis.repository.BusinessPlanRepository
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.PlanRepository
import co.com.kronifyapis.repository.ServiceRepository
import co.com.kronifyapis.repository.UserRepository
import co.com.kronifyapis.utils.ProfileValidationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth

/**
 * Servicio que maneja los planes de suscripcion de los negocios.
 * Asigna planes a negocios y valida los limites de servicios,
 * citas y empleados segun el plan contratado.
 */

@Service
class PlanService(
    private val planRepository: PlanRepository,
    private val businessPlanRepository: BusinessPlanRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository,
    private val appointmentRepository: AppointmentRepository,
    private val employeeRepository: EmployeeRepository,
    private val activationCodeRepository: ActivationCodeRepository,
    private val profileValidationHelper: ProfileValidationHelper
) {

    /**
     * Asigna el plan FREE a un negocio cuando se crea sin planId.
     * Si no existe un plan FREE, lo crea automáticamente con valores por defecto.
     */
    @Transactional
    fun assignFreePlanOnCreate(businessId: Long) {
        ensureDefaultPlans()
        val freePlan = planRepository.findByName("FREE")
            ?: throw ResourceNotFoundException("Plan FREE no encontrado")

        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }

        businessPlanRepository.save(
            BusinessPlan().apply {
                this.business = business
                this.plan = freePlan
                this.active = true
                this.subscriptionStatus = SubscriptionStatus.ACTIVE
                this.startAt = LocalDateTime.now()
            }
        )
    }

    /**
     * Cambia el plan de un negocio. Desactiva el plan actual y asigna el nuevo.
     */
    @Transactional
    fun assignPlan(userId: Long, request: AssignPlanRequest): BusinessPlanResponse {
        val user = profileValidationHelper.requireBusiness(userId)

        val business = businessRepository.findByOwner(user)
            ?: throw ResourceNotFoundException("No tienes un negocio registrado. Solo el propietario del negocio puede asignar planes.")

        val plan = planRepository.findById(request.planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

        if (plan.requiresActivationCode) {
            val code = request.activationCode
                ?: throw BadRequestException("Este plan requiere un código de activación")
            validateAndUseActivationCode(code, plan.planId!!, business.businessId!!)
        }

        validatePlanChangeAllowed(business.businessId!!, plan)

        val currentPlan = businessPlanRepository.findByBusiness_BusinessIdAndActiveTrue(business.businessId!!)
        if (currentPlan != null && currentPlan.plan?.planId == plan.planId) {
            return currentPlan.toResponse()
        }
        if (currentPlan != null) {
            currentPlan.active = false
            currentPlan.subscriptionStatus = SubscriptionStatus.CANCELLED
            currentPlan.endAt = LocalDateTime.now()
            businessPlanRepository.save(currentPlan)
        }

        val newPlan = businessPlanRepository.save(
            BusinessPlan().apply {
                this.business = business
                this.plan = plan
                this.active = true
                this.subscriptionStatus = SubscriptionStatus.ACTIVE
                this.startAt = LocalDateTime.now()
            }
        )

        return newPlan.toResponse()
    }

    /**
     * Obtiene el plan actual del negocio del usuario con el uso que lleva
     */
    @Transactional(readOnly = true)
    fun getCurrentPlan(userId: Long): BusinessPlanUsageResponse {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val business = businessRepository.findByOwner(user)
            ?: throw ResourceNotFoundException("Negocio no encontrado")

        return getBusinessPlanUsage(business.businessId!!)
    }

    /**
     * Calcula el uso del plan para un negocio: cuantos servicios tiene,
     * cuantas citas lleva este mes y cuantos empleados, y si ya llego al limite.
     */
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
            subscriptionStatus = businessPlan.subscriptionStatus,
            startAt = businessPlan.startAt,
            endAt = businessPlan.endAt,
            serviceCount = serviceCount,
            currentMonthAppointmentCount = currentMonthAppointmentCount,
            employeeCount = employeeCount,
            serviceLimitReached = serviceLimitReached,
            appointmentLimitReached = appointmentLimitReached,
            employeeLimitReached = employeeLimitReached,
            serviceLimitExceeded = serviceLimit != null && serviceCount > serviceLimit,
            appointmentLimitExceeded = monthlyLimit != null && currentMonthAppointmentCount > monthlyLimit,
            employeeLimitExceeded = empLimit != null && employeeCount > empLimit
        )
    }

    /**
     * Valida que el negocio no haya superado el limite de servicios del plan.
     * Si no hay limite (plan PRO), deja pasar.
     */
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

    /**
     * Valida que el negocio no haya superado el limite mensual de citas del plan.
     * Cuenta solo las citas no canceladas ni no-show.
     */
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

    /**
     * Valida que el negocio no haya superado el limite de empleados del plan.
     */
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

    fun getAllPlans(): List<PlanResponse> {
        ensureDefaultPlans()
        return planRepository.findAll().map { it.toResponse() }
    }

    fun getPlanById(planId: Long): PlanResponse {
        val plan = planRepository.findById(planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }
        return plan.toResponse()
    }

    @Transactional
    fun createPlan(request: CreatePlanRequest): PlanResponse {
        if (planRepository.findByName(request.name.trim()) != null) {
            throw ConflictException("Ya existe un plan con el nombre '${request.name.trim()}'")
        }

        val plan = planRepository.save(
            Plan().apply {
                name = request.name.trim()
                displayName = request.displayName?.trim()?.takeIf { it.isNotBlank() } ?: request.name.trim()
                description = request.description?.trim()?.takeIf { it.isNotBlank() }
                monthlyPriceCents = request.monthlyPriceCents
                serviceLimit = request.serviceLimit
                monthlyAppointmentLimit = request.monthlyAppointmentLimit
                employeeLimit = request.employeeLimit
                requiresActivationCode = request.requiresActivationCode
            }
        )
        return plan.toResponse()
    }

    @Transactional
    fun updatePlan(planId: Long, request: UpdatePlanRequest): PlanResponse {
        val plan = planRepository.findById(planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

        request.name?.let {
            val trimmed = it.trim()
            val existing = planRepository.findByName(trimmed)
            if (existing != null && existing.planId != planId) {
                throw ConflictException("Ya existe un plan con el nombre '$trimmed'")
            }
            plan.name = trimmed
        }
        request.displayName?.let { plan.displayName = it.trim().takeIf { value -> value.isNotBlank() } ?: plan.name }
        request.description?.let { plan.description = it.trim().takeIf { value -> value.isNotBlank() } }
        request.monthlyPriceCents?.let { plan.monthlyPriceCents = it }
        request.serviceLimit?.let { plan.serviceLimit = it }
        request.monthlyAppointmentLimit?.let { plan.monthlyAppointmentLimit = it }
        request.employeeLimit?.let { plan.employeeLimit = it }
        request.requiresActivationCode?.let { plan.requiresActivationCode = it }

        return planRepository.save(plan).toResponse()
    }

    @Transactional
    fun deletePlan(planId: Long) {
        val plan = planRepository.findById(planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

        if (businessPlanRepository.existsByPlan_PlanIdAndActiveTrue(planId)) {
            throw ConflictException("No se puede eliminar el plan porque hay negocios activos usándolo")
        }

        planRepository.delete(plan)
    }

    @Transactional
    fun createActivationCode(request: CreateActivationCodeRequest): ActivationCodeResponse {
        val plan = planRepository.findById(request.planId)
            .orElseThrow { ResourceNotFoundException("Plan no encontrado") }

        if (!plan.requiresActivationCode) {
            throw BadRequestException("El plan '${plan.name}' no requiere código de activación")
        }

        val code = request.code ?: generateActivationCode()

        if (activationCodeRepository.findByCode(code).isPresent) {
            throw ConflictException("El código de activación ya existe")
        }

        val expiresAt = parseOptionalExpiresAt(request.expiresAt)

        val activationCode = activationCodeRepository.save(
            ActivationCode().apply {
                this.code = code
                this.plan = plan
                this.expiresAt = expiresAt
                this.createdAt = LocalDateTime.now()
            }
        )

        return activationCode.toResponse()
    }

    fun getActivationCodes(used: Boolean?): List<ActivationCodeResponse> {
        val codes = if (used != null) {
            activationCodeRepository.findByUsed(used)
        } else {
            activationCodeRepository.findAll()
        }
        return codes.map { it.toResponse() }
    }

    @Transactional
    fun deleteActivationCode(codeId: Long) {
        val code = activationCodeRepository.findById(codeId)
            .orElseThrow { ResourceNotFoundException("Código de activación no encontrado") }
        activationCodeRepository.delete(code)
    }

    @Transactional
    fun validateAndUseActivationCode(code: String, planId: Long, businessId: Long) {
        val activationCode = activationCodeRepository.findByCode(code)
            .orElseThrow { BadRequestException("Código de activación inválido") }

        if (activationCode.used) {
            throw ConflictException("El código de activación ya fue usado")
        }

        val expiresAt = activationCode.expiresAt
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw BadRequestException("El código de activación ha expirado")
        }

        if (activationCode.plan?.planId != planId) {
            throw BadRequestException("El código no corresponde al plan seleccionado")
        }

        activationCode.used = true
        activationCode.usedAt = LocalDateTime.now()
        activationCode.usedByBusinessId = businessId
        activationCodeRepository.save(activationCode)
    }

    @Transactional
    fun assignPlanToBusiness(businessId: Long, plan: Plan) {
        val business = businessRepository.findById(businessId)
            .orElseThrow { ResourceNotFoundException("Negocio no encontrado") }

        val currentPlan = businessPlanRepository.findByBusiness_BusinessIdAndActiveTrue(businessId)
        if (currentPlan != null) {
            currentPlan.active = false
            currentPlan.endAt = LocalDateTime.now()
            businessPlanRepository.save(currentPlan)
        }

        businessPlanRepository.save(
            BusinessPlan().apply {
                this.business = business
                this.plan = plan
                this.active = true
                this.subscriptionStatus = SubscriptionStatus.ACTIVE
                this.startAt = LocalDateTime.now()
            }
        )
    }

    @Transactional(readOnly = true)
    fun getBusinessPlanHistory(userId: Long): List<BusinessPlanResponse> {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")
        val business = businessRepository.findByOwner(user)
            ?: throw ResourceNotFoundException("Negocio no encontrado")
        return businessPlanRepository.findAllByBusiness_BusinessIdOrderByStartAtDesc(business.businessId!!)
            .map { it.toResponse() }
    }

    private fun Plan.toResponse(): PlanResponse {
        return PlanResponse(
            planId = requireNotNull(planId),
            name = name,
            displayName = displayName.takeIf { it.isNotBlank() } ?: name,
            description = description,
            monthlyPriceCents = monthlyPriceCents,
            serviceLimit = serviceLimit,
            monthlyAppointmentLimit = monthlyAppointmentLimit,
            employeeLimit = employeeLimit,
            requiresActivationCode = requiresActivationCode
        )
    }

    private fun ActivationCode.toResponse(): ActivationCodeResponse {
        return ActivationCodeResponse(
            activationCodeId = requireNotNull(activationCodeId),
            code = code,
            planId = requireNotNull(plan?.planId),
            planName = plan?.name ?: "",
            used = used,
            usedAt = usedAt,
            usedByBusinessId = usedByBusinessId,
            expiresAt = expiresAt,
            createdAt = createdAt
        )
    }

    private fun generateActivationCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val segments = (1..3).map {
            (1..4).map { chars.random() }.joinToString("")
        }
        return segments.joinToString("-")
    }

    private fun parseOptionalExpiresAt(value: String?): LocalDateTime? {
        val normalized = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            LocalDateTime.parse(normalized)
        } catch (_: DateTimeException) {
            try {
                OffsetDateTime.parse(normalized).toLocalDateTime()
            } catch (_: DateTimeException) {
                throw BadRequestException("expiresAt debe tener formato ISO-8601")
            }
        }
    }

    private fun BusinessPlan.toResponse(): BusinessPlanResponse {
        return BusinessPlanResponse(
            plan = requireNotNull(plan).toResponse(),
            active = active,
            subscriptionStatus = subscriptionStatus,
            startAt = startAt,
            endAt = endAt
        )
    }

    private fun validatePlanChangeAllowed(businessId: Long, targetPlan: Plan) {
        val usage = getBusinessPlanUsage(businessId)
        val violations = mutableListOf<String>()

        targetPlan.serviceLimit?.let {
            if (usage.serviceCount > it) violations += "servicios (${usage.serviceCount}/$it)"
        }
        targetPlan.monthlyAppointmentLimit?.let {
            if (usage.currentMonthAppointmentCount > it) violations +=
                "citas del mes (${usage.currentMonthAppointmentCount}/$it)"
        }
        targetPlan.employeeLimit?.let {
            if (usage.employeeCount > it) violations += "empleados (${usage.employeeCount}/$it)"
        }

        if (violations.isNotEmpty()) {
            throw ConflictException(
                "No se puede cambiar a ${targetPlan.name}: el negocio supera los límites de ${violations.joinToString(", ")}"
            )
        }
    }

    @Transactional
    fun ensureDefaultPlans() {
        createOrUpdateDefaultPlan("FREE", "Free", "Plan inicial para validar el negocio", 0, 5, 50, 3, false)
        createOrUpdateDefaultPlan("BASIC", "Basic", "Operación pequeña con agenda digital", 2900000, 10, 200, 5, true)
        createOrUpdateDefaultPlan("PRO", "Pro", "Negocios en crecimiento con mayor capacidad", 7900000, 30, 1000, 15, true)
        createOrUpdateDefaultPlan("PREMIUM", "Premium", "Capacidad avanzada habilitada por activación comercial", 14900000, null, null, null, true)
    }

    private fun createOrUpdateDefaultPlan(
        name: String,
        displayName: String,
        description: String,
        monthlyPriceCents: Int,
        serviceLimit: Int?,
        monthlyAppointmentLimit: Int?,
        employeeLimit: Int?,
        requiresActivationCode: Boolean
    ) {
        val existing = planRepository.findByName(name)
        if (existing != null) {
            existing.displayName = displayName
            existing.description = description
            existing.monthlyPriceCents = monthlyPriceCents
            existing.serviceLimit = serviceLimit
            existing.monthlyAppointmentLimit = monthlyAppointmentLimit
            existing.employeeLimit = employeeLimit
            existing.requiresActivationCode = requiresActivationCode
            planRepository.save(existing)
        } else {
            planRepository.save(
                Plan().apply {
                    this.name = name
                    this.displayName = displayName
                    this.description = description
                    this.monthlyPriceCents = monthlyPriceCents
                    this.serviceLimit = serviceLimit
                    this.monthlyAppointmentLimit = monthlyAppointmentLimit
                    this.employeeLimit = employeeLimit
                    this.requiresActivationCode = requiresActivationCode
                }
            )
        }
    }
}
