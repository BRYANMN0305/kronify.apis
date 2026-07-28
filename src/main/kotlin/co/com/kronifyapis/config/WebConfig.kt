package co.com.kronifyapis.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${app.upload.path}") private val uploadPath: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadDir = Path.of(uploadPath).toAbsolutePath().normalize().toUri().toString()
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadDir)
            .setCachePeriod(3600)
    }
}
