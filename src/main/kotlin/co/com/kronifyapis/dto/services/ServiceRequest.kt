package co.com.kronifyapis.dto.services


import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * DTO que recibe los datos para crear o actualizar un servicio.
 * El nombre no puede estar vacío, la duración debe ser mayor a 0.
 */

data class ServiceRequest(

    @field:NotBlank(message = "El nombre no puede estar vacío")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "La duración no puede estar vacía")
    @field:Positive(message = "La duración debe ser mayor a 0")
    val durationMinutes: Int,

    @field:NotNull(message = "El precio no puede estar vacío")
    @field:Positive(message = "El precio debe ser mayor a 0")
    val price: Double?
)
