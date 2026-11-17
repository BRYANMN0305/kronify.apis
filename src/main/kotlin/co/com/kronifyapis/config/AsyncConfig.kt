package co.com.kronifyapis.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.RejectedExecutionHandler

/**
 * Configuración del executor usado para enviar correos.
 *
 * Es pequeño a propósito: los envíos son informativos y no deben competir
 * con las peticiones de la agenda. Si la cola se llena, el correo se descarta
 * y se registra el aviso; la cita nunca se bloquea ni falla por ello.
 */
@Configuration
@EnableAsync
class AsyncConfig {

    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    @Bean(name = ["mailExecutor"])
    fun mailExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.setCorePoolSize(2)
        executor.setMaxPoolSize(4)
        executor.setQueueCapacity(200)
        executor.setThreadNamePrefix("mail-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(20)
        executor.setRejectedExecutionHandler(
            RejectedExecutionHandler { _, _ ->
                log.warn("La cola de correos está llena; se descartó una notificación")
            }
        )
        return executor
    }
}
