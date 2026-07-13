package co.com.kronifyapis.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRegisterRequest(

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val lastName: String,

    @field:NotBlank
    val phoneNumber: String,

    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    @field:Size(min = 8)
    val passwordHash: String,

    val profileType: String,
)