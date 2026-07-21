package co.com.kronifyapis.dto.appointment

data class AppointmentAutofillResponse(
    val userId: Long,
    val customerId: Long?,
    val name: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String
)
