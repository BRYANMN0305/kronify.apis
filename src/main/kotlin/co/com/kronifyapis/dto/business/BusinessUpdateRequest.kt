package co.com.kronifyapis.dto.business

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class BusinessUpdateRequest(
    val name: String? = null,

    val category: String? = null,

    val description: String? = null,

    val address: String? = null,

    val logoUrl: String? = null,

    @field:Email(message = "El email no es válido")
    val email: String? = null,

    val phoneNumber: String? = null,

    val whatsApp: String? = null,

    val ownerWorksAsEmployee: Boolean? = null
)
