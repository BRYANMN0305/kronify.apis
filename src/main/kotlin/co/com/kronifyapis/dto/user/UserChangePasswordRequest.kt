package co.com.kronifyapis.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserChangePasswordRequest(
    @field:NotBlank(message = "La contraseña actual no puede estar vacía")
    val currentPassword: String,

    @field:NotBlank(message = "La nueva contraseña no puede estar vacía")
    @field:Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    val newPassword: String
)