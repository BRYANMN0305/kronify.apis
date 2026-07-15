package co.com.kronifyapis.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email
    @field:NotBlank (message = "Correo no puede estar vacío")
    val email: String,

    @field:NotBlank (message = "Contraseña no puede estar vacía")
    val password: String
)