package co.com.kronifyapis.dto.publicpage

/**
 * DTO que devuelve la información de un servicio en la página pública del negocio.
 */

data class PublicServiceResponse(
    val serviceId: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: Double?
)