package co.com.kronifyapis.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Redacta y envía los correos informativos de cada evento de una cita.
 *
 * Los envíos se ejecutan fuera de la transacción de la cita y con reintentos.
 * Si un correo falla, solo queda registrado en el log; nunca afecta la cita.
 */
@Service
class AppointmentNotificationService(
    private val emailService: EmailService,
    @Value("\${app.mail.max-attempts:3}") private val maxAttempts: Int
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * Envía todos los correos que correspondan al evento.
     */
    fun dispatch(event: AppointmentNotificationEvent) {
        try {
            val messages = buildMessages(event)
            val sentRecipients = mutableSetOf<String>()

            messages.forEach { message ->
                val recipientKey = message.recipient?.trim()?.lowercase()
                if (recipientKey.isNullOrBlank() || !sentRecipients.add(recipientKey)) return@forEach
                if (emailService.resolveRecipient(message.recipient) == null) return@forEach

                sendWithRetry(event, message)
            }
        } catch (ex: Exception) {
            log.error(
                "No se pudieron preparar los correos del evento {} de la cita {}",
                event.type,
                event.appointmentId,
                ex
            )
        }
    }

    private fun buildMessages(event: AppointmentNotificationEvent): List<MailMessage> {
        val business = event.businessName
        val service = event.serviceName
        val customer = event.customerName
        val date = formatDate(event.startAt)

        return when (event.type) {
            AppointmentNotificationType.PUBLIC_BOOKING_PENDING -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Recibimos tu solicitud de cita en $business",
                    heading = "Recibimos tu solicitud de cita",
                    preheader = "Tu solicitud quedó pendiente de confirmación.",
                    intro = "Recibimos tu solicitud de cita en $business para $service. " +
                        "El negocio revisará la disponibilidad y te confirmará pronto.",
                    note = "Si necesitas cambiar algo, contacta directamente al negocio."
                ),
                MailMessage(
                    recipient = event.employeeEmail,
                    recipientName = event.employeeName,
                    subject = "Nueva cita pendiente: $customer",
                    heading = "Nueva cita pendiente de confirmación",
                    preheader = "Se registró una nueva cita desde la página pública.",
                    intro = "Se registró una nueva cita con $customer para $service. " +
                        "Queda pendiente de confirmación.",
                    note = "Puedes confirmarla o gestionarla desde la agenda."
                )
            )

            AppointmentNotificationType.BUSINESS_BOOKING_CONFIRMED -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Tu cita en $business está confirmada",
                    heading = "Tu cita está confirmada",
                    preheader = "La cita quedó agendada correctamente.",
                    intro = "Tu cita en $business para $service quedó confirmada. " +
                        "Te esperamos el $date.",
                    note = "Si necesitas cambiarla, contacta directamente al negocio."
                ),
                MailMessage(
                    recipient = event.employeeEmail,
                    recipientName = event.employeeName,
                    subject = "Nueva cita agendada: $customer",
                    heading = "Nueva cita agendada",
                    preheader = "Se agendó una cita desde el panel del negocio.",
                    intro = "Se agendó una cita con $customer para $service."
                )
            )

            AppointmentNotificationType.APPOINTMENT_CONFIRMED -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Tu cita en $business fue confirmada",
                    heading = "Tu cita fue confirmada",
                    preheader = "$business confirmó tu cita.",
                    intro = "$business confirmó tu cita para $service. Te esperamos el $date.",
                    note = "Te esperamos."
                )
            )

            AppointmentNotificationType.APPOINTMENT_CANCELLED_BY_BUSINESS -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Tu cita en $business fue cancelada",
                    heading = "Tu cita fue cancelada",
                    preheader = "$business canceló tu cita.",
                    intro = "La cita que tenías en $business para $service fue cancelada.",
                    note = "Si tienes dudas, contacta directamente al negocio.",
                    cancellationReason = event.cancellationReason
                )
            )

            AppointmentNotificationType.APPOINTMENT_CANCELLED_BY_CLIENT -> listOf(
                MailMessage(
                    recipient = event.employeeEmail,
                    recipientName = event.employeeName,
                    subject = "El cliente canceló una cita: $customer",
                    heading = "El cliente canceló una cita",
                    preheader = "$customer canceló su cita.",
                    intro = "$customer canceló la cita de $service programada para el $date.",
                    cancellationReason = event.cancellationReason
                )
            )

            AppointmentNotificationType.APPOINTMENT_RESCHEDULED -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Tu cita en $business fue reprogramada",
                    heading = "Tu cita fue reprogramada",
                    preheader = "La cita tiene una nueva fecha y hora.",
                    intro = "La cita que tenías en $business para $service fue reprogramada. " +
                        "La nueva fecha es el $date.",
                    note = "Si no reconoces este cambio, contacta directamente al negocio."
                ),
                MailMessage(
                    recipient = event.employeeEmail,
                    recipientName = event.employeeName,
                    subject = "Cita reprogramada: $customer",
                    heading = "Cita reprogramada",
                    preheader = "Una cita cambió de fecha y hora.",
                    intro = "La cita con $customer para $service fue reprogramada. " +
                        "La nueva fecha es el $date.",
                    note = "Revisa la agenda para confirmar el nuevo horario."
                )
            )

            AppointmentNotificationType.APPOINTMENT_REMINDER -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Recordatorio: tu cita en $business",
                    heading = "Recordatorio de tu cita",
                    preheader = "Tu cita en $business es en las próximas 24 horas.",
                    intro = "Te recordamos que tienes una cita en $business para $service " +
                        "en las próximas 24 horas.",
                    note = "Te esperamos."
                )
            )

            AppointmentNotificationType.APPOINTMENT_COMPLETED -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Gracias por tu visita a $business",
                    heading = "Gracias por tu visita",
                    preheader = "Tu cita en $business quedó registrada como completada.",
                    intro = "Tu cita de $service en $business quedó registrada como completada. " +
                        "Gracias por visitarnos."
                )
            )

            AppointmentNotificationType.APPOINTMENT_NO_SHOW -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Registro de tu cita en $business",
                    heading = "Registro de tu cita",
                    preheader = "Tu cita en $business quedó registrada como no asistida.",
                    intro = "La cita de $service en $business del $date quedó registrada como no asistida.",
                    note = "Si crees que es un error, contacta directamente al negocio."
                )
            )

            AppointmentNotificationType.APPOINTMENT_AUTO_CANCELLED -> listOf(
                MailMessage(
                    recipient = event.customerEmail,
                    recipientName = customer,
                    subject = "Tu cita en $business fue cancelada",
                    heading = "Tu cita fue cancelada",
                    preheader = "La cita no se confirmó a tiempo y fue cancelada.",
                    intro = "La cita que tenías en $business para $service no fue confirmada a tiempo " +
                        "y fue cancelada automáticamente.",
                    note = "Puedes contactar al negocio para agendar una nueva cita.",
                    cancellationReason = event.cancellationReason ?: "No confirmada a tiempo"
                )
            )
        }
    }

    private fun sendWithRetry(event: AppointmentNotificationEvent, message: MailMessage) {
        val attempts = maxAttempts.coerceAtLeast(1)
        var attempt = 1

        while (true) {
            try {
                emailService.sendHtmlEmail(
                    to = message.recipient ?: return,
                    subject = sanitizeHeader(message.subject),
                    templateName = TEMPLATE_NAME,
                    variables = buildVariables(event, message)
                )
                log.info("Correo de cita enviado: tipo={}, cita={}", event.type, event.appointmentId)
                return
            } catch (ex: Exception) {
                if (attempt >= attempts) {
                    log.error(
                        "No se pudo enviar el correo del evento {} de la cita {} tras {} intentos",
                        event.type,
                        event.appointmentId,
                        attempts,
                        ex
                    )
                    return
                }

                log.warn(
                    "Fallo el envío del correo del evento {} de la cita {} (intento {}/{}): {}",
                    event.type,
                    event.appointmentId,
                    attempt,
                    attempts,
                    ex.message
                )

                try {
                    Thread.sleep(RETRY_BASE_DELAY_MS * attempt)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    log.warn(
                        "Envío de correo interrumpido: tipo={}, cita={}",
                        event.type,
                        event.appointmentId
                    )
                    return
                }
                attempt++
            }
        }
    }

    private fun buildVariables(
        event: AppointmentNotificationEvent,
        message: MailMessage
    ): Map<String, Any?> = mapOf(
        "preheader" to message.preheader,
        "heading" to message.heading,
        "recipientName" to message.recipientName.ifBlank { "cliente" },
        "intro" to message.intro,
        "businessName" to event.businessName,
        "serviceName" to event.serviceName,
        "employeeName" to event.employeeName,
        "dateText" to formatDate(event.startAt),
        "timeText" to "${formatTime(event.startAt)} - ${formatTime(event.endAt)}",
        "durationText" to formatDuration(event.startAt, event.endAt),
        "note" to message.note,
        "cancellationReason" to message.cancellationReason
    )

    private fun sanitizeHeader(value: String): String =
        value.replace(Regex("[\\r\\n]+"), " ").trim()

    private fun formatDate(value: LocalDateTime): String =
        value.format(DATE_FORMATTER).replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(SPANISH_LOCALE) else character.toString()
        }

    private fun formatTime(value: LocalDateTime): String =
        value.format(TIME_FORMATTER)

    private fun formatDuration(startAt: LocalDateTime, endAt: LocalDateTime): String {
        val minutes = ChronoUnit.MINUTES.between(startAt, endAt).coerceAtLeast(0)

        return when {
            minutes == 0L -> ""
            minutes == 1L -> "1 minuto"
            minutes < 60L -> "$minutes minutos"
            minutes % 60L == 0L -> {
                val hours = minutes / 60
                if (hours == 1L) "1 hora" else "$hours horas"
            }
            else -> "${minutes / 60} h ${minutes % 60} min"
        }
    }

    private data class MailMessage(
        val recipient: String?,
        val recipientName: String,
        val subject: String,
        val heading: String,
        val preheader: String,
        val intro: String,
        val note: String? = null,
        val cancellationReason: String? = null
    )

    private companion object {
        const val TEMPLATE_NAME = "appointment-email"
        const val RETRY_BASE_DELAY_MS = 2_000L
        val SPANISH_LOCALE: Locale = Locale.forLanguageTag("es-CO")
        val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", SPANISH_LOCALE)
        val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mm a", SPANISH_LOCALE)
    }
}
