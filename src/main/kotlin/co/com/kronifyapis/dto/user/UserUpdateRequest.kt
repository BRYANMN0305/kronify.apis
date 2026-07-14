package co.com.kronifyapis.dto.user

import jakarta.validation.constraints.NotBlank


data class UserUpdateRequest (

    @field:NotBlank
    val name: String? = null,

    @field:NotBlank
    val lastName: String? = null,

    @field:NotBlank
    val phoneNumber: String? = null,

)