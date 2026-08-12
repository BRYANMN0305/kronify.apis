package co.com.kronifyapis.service

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * Registro de conexiones SSE (Server-Sent Events) por negocio.
 * Mantiene los emitters abiertos de los calendarios conectados y
 * les notifica en tiempo real cuando se crea una cita.
 */
@Component
class AppointmentEventRegistry {

    private val subscribers = ConcurrentHashMap<Long, MutableSet<SseEmitter>>()

    /**
     * Registra un emitter para un negocio y devuelve la conexión lista.
     * La conexión queda abierta de forma indefinida (timeout 0).
     */
    fun subscribe(businessId: Long): SseEmitter {
        val emitter = SseEmitter(0L)
        val set = subscribers.computeIfAbsent(businessId) { ConcurrentHashMap.newKeySet() }
        set.add(emitter)
        emitter.onCompletion { set.remove(emitter) }
        emitter.onTimeout { set.remove(emitter) }
        emitter.onError { set.remove(emitter) }
        try {
            emitter.send(SseEmitter.event().comment("connected"))
        } catch (_: Exception) {
            set.remove(emitter)
        }
        return emitter
    }

    /**
     * Publica el evento de cita creada a todos los calendarios del negocio.
     */
    fun publishAppointmentCreated(businessId: Long, appointment: Any) {
        val set = subscribers[businessId] ?: return
        val payload = SseEmitter.event().name("appointment.created").data(appointment)
        for (emitter in set.toList()) {
            try {
                emitter.send(payload)
            } catch (_: Exception) {
                set.remove(emitter)
            }
        }
    }
}
