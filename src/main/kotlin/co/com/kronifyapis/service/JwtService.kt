package co.com.kronifyapis.service

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.user.ProfileType
import co.com.kronifyapis.model.User
import co.com.kronifyapis.repository.BusinessRepository
import co.com.kronifyapis.repository.EmployeeRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @param:Value("\${app.jwt.secret}") private val secret: String,
    @param:Value("\${app.jwt.expiration-minutes}") val expirationMinutes: Long,
    private val businessRepository: BusinessRepository,
    private val employeeRepository: EmployeeRepository
) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiration = Date(now.time + expirationMinutes * 60_000)

        val business = businessRepository.findByOwner(user)
            ?: employeeRepository.findAllByUser_UserId(user.userId!!)
                .firstOrNull()
                ?.business

        return Jwts.builder()
            .subject(user.userId.toString())
            .claim("name", user.name)
            .claim("lastName", user.lastName)
            .claim("phoneNumber", user.phoneNumber)
            .claim("email", user.email)
            .claim("profileType", user.profileType.name)
            .claim("businessId", business?.businessId)
            .claim("slug", business?.slug)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }

    fun getExpirationSeconds(): Long = expirationMinutes * 60

    fun extractEmail(token: String): String? {
        return extractAllClaims(token)?.get("email", String::class.java)
    }

    fun extractAuthenticatedUser(token: String): AuthenticatedUser? {
        val claims = extractAllClaims(token) ?: return null
        val userId = claims.subject ?: return null
        val email = claims.get("email", String::class.java) ?: return null
        val profileTypeValue = claims.get("profileType", String::class.java) ?: return null

        val businessIdNumber = claims.get("businessId", Number::class.java)?.toLong()
        val slugValue = claims.get("slug", String::class.java)

        return try {
            AuthenticatedUser(
                userId = userId.toLong(),
                email = email,
                profileType = ProfileType.valueOf(profileTypeValue),
                businessId = businessIdNumber,
                slug = slugValue
            )
        } catch (_: IllegalArgumentException) {
            null
        }
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
