package co.com.kronifyapis.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserChangePasswordRequest(
    @field:NotBlank
    val currentPassword: String,

    @field:NotBlank
    @field:Size(min = 8)
    val newPassword: String
)