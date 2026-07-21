package co.com.kronifyapis.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * DTO que recibe los datos para actualizar el perfil de un usuario.
 * Solo se actualizan los campos que se envían.
 */

data class UserUpdateRequest(

    @field:NotBlank (message = "El nombre no puede estar vacío")
    @field:Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre solo puede contener letras")
    val name: String? = null,

    @field:NotBlank (message = "El apellido no puede estar vacío")
    @field:Pattern(regexp = "^[a-zA-Z ]+$", message = "El apellido solo puede contener letras")
    val lastName: String? = null,

    val phoneNumber: String? = null,
)
