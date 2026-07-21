package co.com.kronifyapis.dto.publicpage

data class PublicServiceResponse(
    val serviceId: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val price: Double?
)