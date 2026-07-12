package co.com.kronifyapis.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class HealthController {

    @GetMapping("/")
    fun home(): Map<String, String> {
        return mapOf("message" to "kronify Kotlin API is ready")
    }
}