package co.com.kronifyapis.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

/**
 * Handler que se ejecuta si el flujo OAuth2 falla (usuario canceló,
 * code inválido, proveedor devolvió error, etc.).
 * Redirige al login del frontend con un parámetro de error.
 */
@Component
class OAuth2LoginFailureHandler(
    @param:Value("\${app.frontend-url}") private val frontendUrl: String
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.sendRedirect("$frontendUrl/iniciar-sesion?error=oauth")
    }
}