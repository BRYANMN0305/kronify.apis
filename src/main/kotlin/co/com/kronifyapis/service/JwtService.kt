package co.com.kronifyapis.service

import co.com.kronifyapis.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @param:Value("\${JWT_SECRET}") private val secret: String,
    @param:Value("\${JWT_EXPIRATION_MINUTES}") val expirationMinutes: Long
) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + expirationMinutes * 60_000)

        return Jwts.builder()
            .subject(user.userId.toString())
            .claim("name", user.name)
            .claim("lastName", user.lastName)
            .claim("phoneNumber", user.phoneNumber)
            .claim("email", user.email)
            .claim("profileType", user.profileType.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }
    fun getExpirationSeconds(): Long = expirationMinutes * 60

    fun extractEmail(token: String): String? {
        return extractAllClaims(token)?.get("email", String::class.java)
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractAllClaims(token) != null && !isTokenExpired(token)
        } catch (ex: Exception) {
            false
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractAllClaims(token)?.expiration ?: return true
        return expiration.before(Date())
    }

    private fun extractAllClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: Exception) {
            null
        }
    }
}