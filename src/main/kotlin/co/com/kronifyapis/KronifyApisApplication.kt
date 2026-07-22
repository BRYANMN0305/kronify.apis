package co.com.kronifyapis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Clase principal de la aplicación Kronify Kotlin API.
 * Levanta el contexto de Spring Boot con toda la configuración.
 */
@SpringBootApplication
class KronifyApisApplication

/**
 * Punto de entrada de la aplicación.
 * Inicia el servidor embebido de Spring Boot.
 */
fun main(args: Array<String>) {
    runApplication<KronifyApisApplication>(*args)
}
