package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.LoginRequest
import co.com.kronifyapis.dto.auth.TokenResponse
import co.com.kronifyapis.dto.auth.UserRegisterRequest
import co.com.kronifyapis.dto.user.UserResponse
import co.com.kronifyapis.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Controlador para el registro e inicio de sesión de usuarios.
 */
@RestController
@RequestMapping("/auth")
class AuthController (private val authService: AuthService){

    /**
     * Registra un usuario nuevo en la plataforma.
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserRegisterRequest) : ResponseEntity<UserResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
        .body(authService.register(request))
    }

    /**
     * Inicia sesión con correo y contraseña.
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest) : ResponseEntity<TokenResponse> {
        return ResponseEntity
            .ok(authService.login(request))

    }
}