package co.com.kronifyapis.service

import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Envía el recordatorio de las citas confirmadas que están por comenzar.
 *
 * El recordatorio se envía una sola vez: al detectar la cita se marca
 * reminderSentAt y la siguiente ejecución ya no la vuelve a tomar.
 */
@Service
class AppointmentReminderService(
    private val appointmentRepository: AppointmentRepository,
    private val appointmentNotificationPublisher: AppointmentNotificationPublisher,
    private val emailService: EmailService
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 5 * * * *", zone = COLOMBIA_ZONE_ID)
    @Transactional
    fun sendUpcomingReminders() {
        // Si los correos están apagados no se marca el recordatorio: al volver
        // a habilitarlos, las citas próximas todavía podrán recibirlo.
        if (!emailService.isMailEnabled()) return

        val now = LocalDateTime.now(COLOMBIA_ZONE)
        val reminders = appointmentRepository.findAppointmentsPendingReminder(
            status = AppointmentStatus.CONFIRMED,
            windowStart = now,
            windowEnd = now.plusHours(REMINDER_HOURS)
        )

        if (reminders.isEmpty()) return

        reminders.forEach { it.reminderSentAt = now }
        appointmentRepository.saveAll(reminders)

        // El listener espera el commit para enviar, así que la marca y el correo
        // quedan consistentes: si la transacción falla, no se envía nada.
        reminders.forEach {
            appointmentNotificationPublisher.publish(it, AppointmentNotificationType.APPOINTMENT_REMINDER)
        }

        log.info("Se programaron {} recordatorios de citas confirmadas", reminders.size)
    }

    private companion object {
        const val COLOMBIA_ZONE_ID = "America/Bogota"
        const val REMINDER_HOURS = 24L
        val COLOMBIA_ZONE: ZoneId = ZoneId.of(COLOMBIA_ZONE_ID)
    }
}
