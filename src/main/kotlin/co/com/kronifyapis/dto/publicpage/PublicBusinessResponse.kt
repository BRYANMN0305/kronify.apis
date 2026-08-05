package co.com.kronifyapis.dto.publicpage

import co.com.kronifyapis.dto.business.OpeningHourResponse

/**
 * DTO que devuelve la información completa de un negocio para la página pública,
 * incluyendo sus servicios, empleados disponibles y horario de atención.
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
    val employees: List<PublicEmployeeResponse>,
    val openingHours: List<OpeningHourResponse>
)