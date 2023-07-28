package top.bettercode.summer.security

import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.keygen.KeyGenerators
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.security.repository.ApiTokenRepository
import top.bettercode.summer.security.token.ApiAccessToken
import top.bettercode.summer.security.token.ApiToken
import top.bettercode.summer.security.token.InstantAt
import top.bettercode.summer.security.token.Token
import top.bettercode.summer.security.userdetails.*
import java.time.Instant
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
class ApiTokenService(
        val securityProperties: ApiSecurityProperties,
        val apiTokenRepository: ApiTokenRepository,
        private val userDetailsService: UserDetailsService
) : NeedKickedOutValidator, UserDetailsValidator, LoginListener {
    fun createAccessToken(): Token {
        val tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (apiTokenRepository.findByAccessToken(tokenValue) != null) {
            return createAccessToken()
        }
        val now = Instant.now()
        val accessTokenValiditySeconds = securityProperties.accessTokenValiditySeconds
        return Token(tokenValue, now,
                if (accessTokenValiditySeconds > 0) now.plusSeconds(
                        accessTokenValiditySeconds.toLong()) else null)
    }

    fun createRefreshToken(): Token {
        val tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (apiTokenRepository.findByRefreshToken(tokenValue) != null) {
            return createRefreshToken()
        }
        val now = Instant.now()
        val refreshTokenValiditySeconds = securityProperties.refreshTokenValiditySeconds
        return Token(tokenValue, now,
                if (refreshTokenValiditySeconds > 0) now.plusSeconds(
                        refreshTokenValiditySeconds.toLong()) else null)
    }

    fun createUserDetailsInstantAt(): InstantAt {
        val now = Instant.now()
        val userDetailsValiditySeconds = securityProperties.userDetailsValiditySeconds
        return InstantAt(now,
                if (userDetailsValiditySeconds > 0) now.plusSeconds(
                        userDetailsValiditySeconds.toLong()) else null)
    }

    fun getApiAccessToken(scope: String, username: String): ApiAccessToken {
        val userDetails = getUserDetails(scope, username)
        return getApiAccessToken(scope, userDetails, validate(scope, userDetails))
    }

    fun getApiAccessToken(scope: String, username: String, loginKickedOut: Boolean): ApiAccessToken {
        val userDetails = getUserDetails(scope, username)
        return getApiAccessToken(scope, userDetails, loginKickedOut)
    }

    fun refreshUserDetails(scope: String, oldUsername: String?, newUsername: String?) {
        val oldUserDetails = getUserDetails(scope, oldUsername)
        val apiToken = getApiToken(scope, oldUserDetails, false)
        apiToken.userDetailsInstantAt = createUserDetailsInstantAt()
        apiToken.userDetails = getUserDetails(scope, newUsername)
        apiTokenRepository.save(apiToken)
    }

    fun refreshUserDetails(scope: String, username: String?) {
        val userDetails = getUserDetails(scope, username)
        getApiAccessToken(scope, userDetails)
    }

    fun getApiAccessToken(scope: String, userDetails: UserDetails): ApiAccessToken {
        return getApiAccessToken(scope, userDetails, validate(scope, userDetails))
    }

    fun getApiAccessToken(
            scope: String, userDetails: UserDetails,
            loginKickedOut: Boolean
    ): ApiAccessToken {
        val authenticationToken = getApiToken(scope, userDetails, loginKickedOut)
        apiTokenRepository.save(authenticationToken)
        return authenticationToken.toApiToken()
    }

    fun getApiToken(scope: String, userDetails: UserDetails): ApiToken {
        return getApiToken(scope, userDetails, validate(scope, userDetails))
    }

    fun getApiToken(scope: String, userDetails: UserDetails, loginKickedOut: Boolean): ApiToken {
        var apiToken: ApiToken?
        if (loginKickedOut) {
            apiToken = ApiToken(scope, createAccessToken(),
                    createRefreshToken(), createUserDetailsInstantAt(), userDetails)
        } else {
            apiToken = apiTokenRepository.findByScopeAndUsername(scope, userDetails.username)
            if (apiToken == null || apiToken.refreshToken.isExpired) {
                apiToken = ApiToken(scope, createAccessToken(),
                        createRefreshToken(), createUserDetailsInstantAt(), userDetails)
            } else if (apiToken.accessToken.isExpired) {
                apiToken.accessToken = createAccessToken()
                apiToken.userDetailsInstantAt = createUserDetailsInstantAt()
                apiToken.userDetails = userDetails
            } else {
                apiToken.userDetailsInstantAt = createUserDetailsInstantAt()
                apiToken.userDetails = userDetails
            }
        }
        return apiToken
    }

    fun getUserDetails(grantType: String?, request: HttpServletRequest?): UserDetails {
        if (userDetailsService is GrantTypeUserDetailsService) {
            return userDetailsService.loadUserByGrantTypeAndRequest(
                    grantType, request)
        }
        throw IllegalArgumentException("不支持的grantType类型")
    }

    fun getUserDetails(scope: String?, username: String?): UserDetails {
        val userDetails: UserDetails = if (userDetailsService is ScopeUserDetailsService) {
            userDetailsService.loadUserByScopeAndUsername(
                    scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        return userDetails
    }

    fun removeApiToken(scope: String, username: String) {
        apiTokenRepository.remove(scope, username)
    }

    fun removeApiToken(scope: String, usernames: List<String>) {
        apiTokenRepository.remove(scope, usernames)
    }

    override fun beforeLogin(request: HttpServletRequest?, grantType: String?, scope: String?) {
        if (userDetailsService is LoginListener) {
            (userDetailsService as LoginListener).beforeLogin(request, grantType, scope)
        }
    }

    override fun afterLogin(apiToken: ApiToken?, request: HttpServletRequest?) {
        if (userDetailsService is LoginListener) {
            (userDetailsService as LoginListener).afterLogin(apiToken, request)
        }
    }

    override fun validate(userDetails: UserDetails) {
        if (userDetailsService is UserDetailsValidator) {
            (userDetailsService as UserDetailsValidator).validate(userDetails)
        }
    }

    override fun validate(scope: String, userDetails: UserDetails): Boolean {
        return if (userDetailsService is NeedKickedOutValidator) {
            (userDetailsService as NeedKickedOutValidator).validate(scope,
                    userDetails)
        } else {
            securityProperties.needKickedOut(scope)
        }
    }

    companion object {
        private val DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20)
    }
}
