package co.com.kronifyapis.repository

import co.com.kronifyapis.model.OauthAccount
import org.springframework.data.jpa.repository.JpaRepository

interface OauthAccountRepository : JpaRepository<OauthAccount, Long> {

    fun findAllByUser_UserId(userId: Long): List<OauthAccount>

    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): OauthAccount?
}
