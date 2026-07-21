package co.com.kronifyapis.dto.business

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * DTO que recibe los datos para crear un nuevo negocio.
 * Incluye información básica y si el dueño también trabajará como empleado.
 */

data class BusinessCreateRequest(
    @field:NotBlank (message = "El nombre es obligatorio")
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "El slug debe ser alfanumérico y separado por guiones")
    val slug: String,

    val category: String? = null,

    val description: String? = null,

    val address: String? = null,

    val logoUrl: String? = null,

    @field:Email(message = "El email no es válido")
    val email: String? = null,

    val phoneNumber: String? = null,

    val whatsApp: String? = null,

    val ownerWorksAsEmployee: Boolean = true
)
