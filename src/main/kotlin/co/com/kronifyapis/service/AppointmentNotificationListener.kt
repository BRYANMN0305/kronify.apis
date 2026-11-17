package co.com.kronifyapis.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Recibe los eventos de cita después del commit y los envía en segundo plano.
 *
 * La anotación @Async evita que el hilo de la petición espere al servidor SMTP.
 */
@Component
class AppointmentNotificationListener(
    private val appointmentNotificationService: AppointmentNotificationService
) {
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onAppointmentNotification(event: AppointmentNotificationEvent) {
        appointmentNotificationService.dispatch(event)
    }
}
