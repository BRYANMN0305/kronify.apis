package co.com.kronifyapis.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuración web de la aplicación.
 * Los archivos subidos ya no se sirven como recursos estáticos crudos:
 * se sirven a través de UploadController con cabeceras de seguridad
 * (nosniff, CSP sandbox) y Content-Type restringido por extensión.
 */
@Configuration
class WebConfig : WebMvcConfigurer
