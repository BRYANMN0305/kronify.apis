package co.com.kronifyapis.service

import co.com.kronifyapis.model.enums.AppointmentStatus
import co.com.kronifyapis.repository.AppointmentRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Cierre automático de citas que nadie resolvió.
 *
 * Una cita PENDING que llegó a su hora y sigue pendiente no puede confirmarse
 * (la confirmación solo vale antes de empezar). Para que no quede eternamente
 * en PENDING, este proceso la cancela cuando ya pasó el margen de gracia.
 *
 * El negocio conserva todo ese margen para registrar manualmente si el cliente
 * asistió (COMPLETED), no asistió (NO_SHOW) o si la cita se canceló.
 */
@Service
class AppointmentExpiryService(
    private val appointmentRepository: AppointmentRepository,
    private val appointmentNotificationPublisher: AppointmentNotificationPublisher
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * Corre cada hora. Cancela las citas PENDING cuya hora de inicio quedó
     * más de 24 horas atrás, dejando un día completo para el cierre manual.
     */
    @Scheduled(cron = "0 15 * * * *", zone = COLOMBIA_ZONE_ID)
    @Transactional
    fun expireUnresolvedPendingAppointments() {
        val now = LocalDateTime.now(COLOMBIA_ZONE)
        val cutoff = now.minusHours(GRACE_HOURS)
        val expiring = appointmentRepository.findExpiredAppointments(AppointmentStatus.PENDING, cutoff)
        // El evento se arma antes del update en bloque, mientras las relaciones
        // de cada cita todavía están cargadas.
        expiring.forEach {
            appointmentNotificationPublisher.publish(
                it,
                AppointmentNotificationType.APPOINTMENT_AUTO_CANCELLED,
                CANCELLATION_REASON
            )
        }
        val closed = appointmentRepository.closeExpiredAppointments(
            currentStatus = AppointmentStatus.PENDING,
            newStatus = AppointmentStatus.CANCELLED,
            reason = CANCELLATION_REASON,
            cutoff = cutoff,
            now = now
        )
        if (closed > 0) {
            log.info("Se cancelaron {} citas pendientes que superaron el margen de cierre", closed)
        }
    }

    private companion object {
        const val COLOMBIA_ZONE_ID = "America/Bogota"
        const val GRACE_HOURS = 24L
        const val CANCELLATION_REASON = "No confirmada a tiempo"
        val COLOMBIA_ZONE: ZoneId = ZoneId.of(COLOMBIA_ZONE_ID)
    }
}
