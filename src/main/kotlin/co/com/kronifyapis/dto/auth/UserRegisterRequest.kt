package co.com.kronifyapis.dto.auth

import co.com.kronifyapis.dto.user.ProfileType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserRegisterRequest(

    @field:NotBlank (message = "El nombre es obligatorio")
    val name: String,

    @field:NotBlank (message = "El apellido es obligatorio")
    val lastName: String,

    @field:NotBlank (message = "El número de teléfono es obligatorio")
    val phoneNumber: String,

    @field:Email
    @field:NotBlank (message = "Correo no puede estar vacío")
    val email: String,

    @field:NotBlank (message = "Contraseña no puede estar vacía")
    @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    val passwordHash: String,

    @field:NotNull(message = "El tipo de perfil es obligatorio")
    val profileType: ProfileType,
)
