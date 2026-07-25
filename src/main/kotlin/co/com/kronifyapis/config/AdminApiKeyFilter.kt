package co.com.kronifyapis.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AdminApiKeyFilter(
    @Value("\${app.admin.api-key}") private val adminApiKey: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key")
        if (apiKey != adminApiKey) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("""{"error": "API key inválida o no proporcionada"}""")
            return
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/admin/")
    }
}
