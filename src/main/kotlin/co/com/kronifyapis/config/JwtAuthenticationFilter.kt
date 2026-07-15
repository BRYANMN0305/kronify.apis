package co.com.kronifyapis.config

import co.com.kronifyapis.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()
        val authenticatedUser = jwtService.extractAuthenticatedUser(token)

        if (authenticatedUser != null && jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
                val authorities = listOf(SimpleGrantedAuthority("PROFILE_TYPE_${authenticatedUser.profileType.name}"))
                val authentication = UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
