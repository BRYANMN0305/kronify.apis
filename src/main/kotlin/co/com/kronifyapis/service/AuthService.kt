package co.com.kronifyapis.service

import co.com.kronifyapis.dto.auth.LoginRequest
import co.com.kronifyapis.dto.auth.LinkedAuthMethodResponse
import co.com.kronifyapis.dto.auth.OAuthLoginResult
import co.com.kronifyapis.dto.auth.TokenResponse
import co.com.kronifyapis.dto.auth.UserRegisterRequest
import co.com.kronifyapis.model.enums.StatusType

import co.com.kronifyapis.exception.BadRequestException
import co.com.kronifyapis.exception.ConflictException
import co.com.kronifyapis.exception.InvalidCredentialsException
import co.com.kronifyapis.exception.ResourceNotFoundException
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.OauthAccount
import co.com.kronifyapis.model.User
import co.com.kronifyapis.model.enums.ProfileType
import co.com.kronifyapis.repository.EmployeeInvitationRepository
import co.com.kronifyapis.repository.EmployeeRepository
import co.com.kronifyapis.repository.OauthAccountRepository
import co.com.kronifyapis.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
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
    fun register(request: UserRegisterRequest): TokenResponse {
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

        linkInvitationIfNeeded(savedUser)

        val token = jwtService.generateToken(savedUser)
        return savedUser.toTokenResponse(token, jwtService.getExpirationSeconds())
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
        // Las cuentas creadas solo por OAuth tienen passwordHash vacío, por lo que
        // passwordEncoder.matches falla naturalmente y no pueden entrar por contraseña.

        val token = jwtService.generateToken(user)
        return user.toTokenResponse(token, jwtService.getExpirationSeconds())
    }

    /**
     * Inicia/login mediante un proveedor OAuth (Google, Microsoft).
     * Si el email ya existe, vincula la cuenta OAuth a ese usuario (email verificado
     * por el proveedor = misma identidad). Si no existe, crea el usuario con
     * passwordHash vacío (no podrá iniciar sesión por contraseña) y enlaza
     * invitaciones pendientes como en el registro normal.
     */
    @Transactional
    fun loginWithOAuth(
        provider: String,
        providerUserId: String,
        email: String?,
        name: String?,
        lastName: String?
    ): OAuthLoginResult {
        val normalizedEmail = email?.trim()?.lowercase()
        if (normalizedEmail.isNullOrBlank()) {
            throw BadRequestException("El proveedor no devolvió un correo")
        }

        var user = userRepository.findByEmail(normalizedEmail)
        val newUser = user == null

        if (user == null) {
            user = userRepository.save(
                User(
                    name = name?.takeIf { it.isNotBlank() } ?: "Usuario",
                    lastName = lastName?.takeIf { it.isNotBlank() } ?: "",
                    email = normalizedEmail,
                    profileType = ProfileType.CLIENT,
                    passwordHash = "",
                    verifiedEmail = true
                )
            )
            linkInvitationIfNeeded(user)
        } else {
            if (!user.active) {
                throw InvalidCredentialsException("Tu cuenta está desactivada")
            }
            if (!user.verifiedEmail) {
                user.verifiedEmail = true
                userRepository.save(user)
            }
        }

        linkOAuthAccountIfMissing(user, provider, providerUserId, normalizedEmail)

        val token = jwtService.generateToken(user)
        return OAuthLoginResult(
            token = user.toTokenResponse(token, jwtService.getExpirationSeconds()),
            newUser = newUser
        )
    }

    /**
     * Actualiza el tipo de perfil elegido por un usuario nuevo de OAuth
     * y emite un token nuevo con los claims actualizados.
     */
    @Transactional
    fun selectOAuthProfile(userId: Long, profileType: ProfileType): TokenResponse {
        val user = userRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Usuario no encontrado")

        user.profileType = profileType
        userRepository.save(user)

        val token = jwtService.generateToken(user)
        return user.toTokenResponse(token, jwtService.getExpirationSeconds())
    }

    /**
     * Vincula la cuenta del proveedor al usuario si aún no está vinculada.
     * Ignora conflictos de unicidad (solicitudes concurrentes / re-vinculación).
     */
    private fun linkOAuthAccountIfMissing(user: User, provider: String, providerUserId: String, email: String) {
        if (oauthAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) return
        if (oauthAccountRepository.existsByProviderAndUser_UserId(provider, user.userId!!)) return

        try {
            oauthAccountRepository.save(
                OauthAccount().apply {
                    this.user = user
                    this.provider = provider
                    this.providerUserId = providerUserId
                    this.providerEmail = email
                }
            )
        } catch (_: DataIntegrityViolationException) {
            // Ya vinculada en otra solicitud: se ignora
        }
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

        if (employeeRepository.existsByUserAndBusinessAndActiveTrue(user, invitation.business!!)) return

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
