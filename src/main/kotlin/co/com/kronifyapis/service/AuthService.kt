package co.com.kronifyapis.service

import co.com.kronifyapis.dto.auth.LoginRequest
import co.com.kronifyapis.dto.auth.LinkedAuthMethodResponse
import co.com.kronifyapis.dto.auth.TokenResponse
import co.com.kronifyapis.dto.auth.UserRegisterRequest
import co.com.kronifyapis.model.enums.StatusType
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.dto.user.UserResponse
import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.EmployeeInvitationRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.OauthAccountRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


/**
 * Servicio de autenticacion: registra usuarios, inicia sesion,
 * consulta metodos de autenticacion vinculados y enlaza invitaciones
 * pendientes cuando alguien se registra.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val employeeInvitationRepository: EmployeeInvitationRepository,
    private val employeeRepository: EmployeeRepository,
    private val oauthAccountRepository: OauthAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val planService: PlanService
) {
    /**
     * Registra un usuario nuevo en el sistema.
     * Valida que el email no exista, que la contrasena tenga al menos 8 caracteres,
     * y si el usuario es de tipo BUSINESS, revisa si tiene invitacion pendiente
     * para vincularlo automaticamente como empleado.
     */
    @Transactional
    fun register(request: UserRegisterRequest): UserResponse {
        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("El email ya se encuentra registrado")
        }

        if (request.passwordHash.length < 8) {
            throw BadRequestException("La contraseña debe tener al menos 8 caracteres")
        }

        val user = User(
            name = request.name,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber.trim(),
            email = email,
            passwordHash = requireNotNull(passwordEncoder.encode(request.passwordHash)),
            profileType = request.profileType,
        )
        val savedUser = userRepository.save(user)

        if (savedUser.profileType == ProfileType.CLIENT) {
            return savedUser.toResponse()
        }

        linkInvitationIfNeeded(savedUser)
        return savedUser.toResponse()
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse(
            userId = requireNotNull(userId),
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,

            profileType = profileType,
            active = active,
            createdAt = createdAt,
        )
    }

    /**
     * Inicia sesion con email y contrasena.
     * Si las credenciales son correctas, genera un token JWT y lo devuelve.
     */
    fun login(request: LoginRequest): TokenResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("Correo o contraseña incorrectas")

        if (!user.active || !passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException("Correo o contraseña incorrectas")
        }

        val token = jwtService.generateToken(user)
        return user.toTokenResponse(token, jwtService.getExpirationSeconds())
    }

    /**
     * Lista los metodos de autenticacion vinculados a un usuario:
     * contrasena (password) y cuentas OAuth (Google, etc.).
     */
    @Transactional(readOnly = true)
    fun listLinkedAuthMethods(userId: Long): List<LinkedAuthMethodResponse> {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        val linkedOauthAccounts = oauthAccountRepository.findAllByUser_UserId(user.userId!!)
        val passwordLinked = user.passwordHash.isNotBlank()

        return buildList {
            if (passwordLinked) {
                add(
                    LinkedAuthMethodResponse(
                        type = "PASSWORD",
                        provider = null,
                        email = user.email,
                        linkedAt = user.createdAt
                    )
                )
            }

            linkedOauthAccounts.forEach { account ->
                add(
                    LinkedAuthMethodResponse(
                        type = "OAUTH",
                        provider = account.provider,
                        email = account.providerEmail ?: user.email,
                        linkedAt = account.createdAt
                    )
                )
            }
        }
    }

    private fun User.toTokenResponse(token: String, expiresIn: Long): TokenResponse {
        return TokenResponse(
            accessToken = token,
            expiresIn = expiresIn
        )
    }

    /**
     * Si el usuario se registro con un email que tiene una invitacion pendiente,
     * lo vincula automaticamente como empleado del negocio que lo invito.
     * Si la invitacion ya expiro, la marca como EXPIRED.
     */
    private fun linkInvitationIfNeeded(user: User) {
        val invitation = employeeInvitationRepository.findFirstByEmailAndStatus(user.email, StatusType.PENDING)
            ?: return

        if (invitation.expiresAt.isBefore(LocalDateTime.now())) {
            invitation.status = StatusType.EXPIRED
            employeeInvitationRepository.save(invitation)
            return
        }

        if (employeeRepository.existsByUserAndBusiness(user, invitation.business!!)) return

        planService.validateEmployeeLimit(invitation.business!!.businessId!!)

        employeeRepository.save(
            Employee().apply {
                this.user = user
                this.business = invitation.business
                this.owner = false
                selfManagedSchedule = true
                active = true
            }
        )

        invitation.status = StatusType.ACCEPTED
        invitation.acceptedBy = user
        invitation.acceptedAt = LocalDateTime.now()
        employeeInvitationRepository.save(invitation)
    }
}
