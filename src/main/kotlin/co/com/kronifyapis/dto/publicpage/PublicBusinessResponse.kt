package co.com.kronifyapis.dto.publicpage

/**
 * DTO que devuelve la información completa de un negocio para la página pública,
 * incluyendo sus servicios y empleados disponibles.
 */

data class PublicBusinessResponse(
    val businessId: Long,
    val name: String,
    val slug: String,
    val category: String?,
    val description: String?,
    val address: String?,
    val logoUrl: String?,
    val phoneNumber: String?,
    val whatsapp: String?,
    val services: List<PublicServiceResponse>,
    val employees: List<PublicEmployeeResponse>
)