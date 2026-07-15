package co.com.kronifyapis.dto.business

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class BusinessCreateRequest(
    @field:NotBlank (message = "El nombre es obligatorio")
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",)
    val slug: String,

    @field:NotBlank (message = "La categoría es obligatoria")
    val category: String? = null,

    val description: String? = null,

    val address: String? = null,

    val logoUrl: String? = null,

    @field:NotBlank (message = "El email es obligatorio")
    @field:Email(message = "El email no es válido")
    val email: String? = null,

    @field:NotBlank (message = "El número de teléfono es obligatorio")
    val phoneNumber: String? = null,

    @field:NotBlank (message = "El número de WhatsApp es obligatorio")
    val whatsApp: String? = null
)