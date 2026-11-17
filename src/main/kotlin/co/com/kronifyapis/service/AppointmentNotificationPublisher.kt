package co.com.kronifyapis.service

import co.com.kronifyapis.model.Appointment
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Prepara y publica las notificaciones de citas.
 *
 * Se ejecuta dentro de la transacción del cambio de cita para poder leer las
 * relaciones de la entidad, pero el envío real se difiere con
 * [AppointmentNotificationListener] hasta que la transacción haya hecho commit.
 */
@Component
class AppointmentNotificationPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * Publica el evento de notificación de una cita.
     *
     * Cualquier error al preparar el correo se registra y se ignora: una
     * notificación nunca debe hacer fallar la creación o gestión de la cita.
     */
    fun publish(
        appointment: Appointment,
        type: AppointmentNotificationType,
        cancellationReason: String? = appointment.cancellationReason
    ) {
        try {
            val appointmentId = appointment.appointmentId ?: return
            val business = appointment.business ?: return
            val service = appointment.service ?: return
            val employee = appointment.employee ?: return
            val customer = appointment.customer ?: return
            val employeeUser = employee.user
            val hasCustomerName = !customer.name.isNullOrBlank() || !customer.lastName.isNullOrBlank()
            val customerFirstName = if (hasCustomerName) customer.name else customer.user?.name
            val customerLastName = if (hasCustomerName) customer.lastName else customer.user?.lastName

            applicationEventPublisher.publishEvent(
                AppointmentNotificationEvent(
                    type = type,
                    appointmentId = appointmentId,
                    businessName = business.name.trim().ifBlank { "el negocio" },
                    serviceName = service.name.trim().ifBlank { "el servicio" },
                    employeeName = fullName(employeeUser?.name, employeeUser?.lastName, "el empleado"),
                    employeeEmail = employeeUser?.email?.trim()?.takeIf { it.isNotBlank() },
                    customerName = fullName(customerFirstName, customerLastName, "el cliente"),
                    customerEmail = customer.email?.trim()?.takeIf { it.isNotBlank() }
                        ?: customer.user?.email?.trim()?.takeIf { it.isNotBlank() },
                    startAt = appointment.startAt,
                    endAt = appointment.endAt,
                    cancellationReason = cancellationReason?.trim()?.takeIf { it.isNotBlank() }
                )
            )
        } catch (ex: Exception) {
            log.warn(
                "No se pudo preparar la notificación {} de la cita {}",
                type,
                appointment.appointmentId,
                ex
            )
        }
    }

    private fun fullName(first: String?, last: String?, fallback: String): String {
        val name = listOfNotNull(
            first?.trim()?.takeIf { it.isNotBlank() },
            last?.trim()?.takeIf { it.isNotBlank() }
        ).joinToString(" ")
        return name.ifBlank { fallback }
    }
}
