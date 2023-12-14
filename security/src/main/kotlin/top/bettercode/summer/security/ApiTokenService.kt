package top.bettercode.summer.security

import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.keygen.KeyGenerators
import top.bettercode.summer.security.client.ClientDetails
import top.bettercode.summer.security.client.ClientDetailsService
import top.bettercode.summer.security.config.ApiSecurityProperties
import top.bettercode.summer.security.repository.StoreTokenRepository
import top.bettercode.summer.security.token.*
import top.bettercode.summer.security.userdetails.*
import java.time.Instant
import javax.servlet.http.HttpServletRequest

/**
 * @author Peter Wu
 */
class ApiTokenService(
        val securityProperties: ApiSecurityProperties,
        val storeTokenRepository: StoreTokenRepository,
        val clientDetailsService: ClientDetailsService,
        val accessTokenConverter: AccessTokenConverter,
        private val userDetailsService: UserDetailsService
) : NeedKickedOutValidator, UserDetailsValidator, LoginListener {

    fun createAccessToken(clientDetails: ClientDetails): Token {
        val tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (storeTokenRepository.findByAccessToken(tokenValue) != null) {
            return createAccessToken(clientDetails)
        }
        val now = Instant.now()
        val accessTokenValiditySeconds = clientDetails.accessTokenValiditySeconds
        return Token(tokenValue, now,
                if (accessTokenValiditySeconds > 0) now.plusSeconds(
                        accessTokenValiditySeconds.toLong()) else null)
    }

    private fun createRefreshToken(clientDetails: ClientDetails): Token {
        val tokenValue = Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (storeTokenRepository.findByRefreshToken(tokenValue) != null) {
            return createRefreshToken(clientDetails)
        }
        val now = Instant.now()
        val refreshTokenValiditySeconds = clientDetails.refreshTokenValiditySeconds
        return Token(tokenValue, now,
                if (refreshTokenValiditySeconds > 0) now.plusSeconds(
                        refreshTokenValiditySeconds.toLong()) else null)
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: Set<String>, username: String): IAccessToken {
        val userDetails = getUserDetails(clientId, scope, username)
        return getAccessToken(clientId, scope, userDetails, validate(clientId, scope, userDetails))
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: String, username: String): IAccessToken {
        return getAccessToken(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: Set<String>, username: String, loginKickedOut: Boolean): IAccessToken {
        val userDetails = getUserDetails(clientId, scope, username)
        return getAccessToken(clientId, scope, userDetails, loginKickedOut)
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: String, username: String, loginKickedOut: Boolean): IAccessToken {
        return getAccessToken(clientId, setOf(scope), username, loginKickedOut)
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: Set<String>, userDetails: UserDetails, loginKickedOut: Boolean = validate(clientId, scope, userDetails)): IAccessToken {
        val storeToken = getStoreToken(clientId, scope, userDetails, loginKickedOut)
        storeTokenRepository.save(storeToken)
        return accessTokenConverter.convert(storeToken)
    }

    @JvmOverloads
    fun getAccessToken(clientId: String = getClientId(), scope: String, userDetails: UserDetails, loginKickedOut: Boolean = validate(clientId, setOf(scope), userDetails)): IAccessToken {
        return getAccessToken(clientId, setOf(scope), userDetails, loginKickedOut)
    }

    @JvmOverloads
    fun refreshUserDetails(clientId: String = getClientId(), scope: Set<String>, oldUsername: String, newUsername: String) {
        val oldUserDetails = getUserDetails(clientId, scope, oldUsername)
        val apiToken = getStoreToken(clientId, scope, oldUserDetails, false)
        apiToken.userDetails = getUserDetails(clientId, scope, newUsername)
        storeTokenRepository.save(apiToken)
    }

    @JvmOverloads
    fun refreshUserDetails(clientId: String = getClientId(), scope: String, username: String) {
        refreshUserDetails(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun refreshUserDetails(clientId: String = getClientId(), scope: Set<String>, username: String) {
        val userDetails = getUserDetails(clientId, scope, username)
        getAccessToken(clientId, scope, userDetails)
    }

    @JvmOverloads
    fun getStoreToken(clientId: String = getClientId(), scope: String, userDetails: UserDetails, loginKickedOut: Boolean = validate(clientId, setOf(scope), userDetails)): StoreToken {
        return getStoreToken(clientId, setOf(scope), userDetails, loginKickedOut)
    }


    @JvmOverloads
    fun getStoreToken(clientId: String = getClientId(), scope: Set<String>, userDetails: UserDetails, loginKickedOut: Boolean = validate(clientId, scope, userDetails)): StoreToken {
        val clientDetails = getClientDetails(clientId)
        val isScopeClientId = securityProperties.isScopeClientId && clientDetailsService.isSingleClient
        //兼容
        val tokenId =
                if (isScopeClientId && scope.isNotEmpty()) {
                    TokenId(clientId = scope.joinToString(","), username = userDetails.username)
                } else {
                    TokenId(clientId = clientId, username = userDetails.username)
                }

        var storeToken: StoreToken?
        if (loginKickedOut) {
            storeToken = StoreToken(id = tokenId, clientId = clientId, scope = scope, accessToken = createAccessToken(clientDetails),
                    refreshToken = createRefreshToken(clientDetails), userDetails = userDetails)
        } else {
            storeToken = storeTokenRepository.findById(tokenId)
            if (storeToken == null || storeToken.refreshToken.isExpired) {
                storeToken = StoreToken(tokenId, clientId = clientId, scope = scope, accessToken = createAccessToken(clientDetails),
                        refreshToken = createRefreshToken(clientDetails), userDetails = userDetails)
            } else if (storeToken.accessToken.isExpired) {
                storeToken.accessToken = createAccessToken(clientDetails)
                storeToken.userDetails = userDetails
            } else {
                storeToken.userDetails = userDetails
            }
        }
        return storeToken
    }

    fun getUserDetails(grantType: String, request: HttpServletRequest): UserDetails {
        if (userDetailsService is GrantTypeUserDetailsService) {
            return userDetailsService.loadUserByGrantTypeAndRequest(
                    grantType, request)
        }
        throw IllegalArgumentException("不支持的grantType类型")
    }

    @JvmOverloads
    fun getUserDetails(clientId: String = getClientId(), scope: String, username: String): UserDetails {
        return getUserDetails(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun getUserDetails(clientId: String = getClientId(), scope: Set<String>, username: String): UserDetails {
        val userDetails: UserDetails = if (userDetailsService is ClientUserDetailsService) {
            userDetailsService.loadUserByClientAndUsername(clientId, scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        return userDetails
    }

    @JvmOverloads
    fun removeToken(clientId: String = getClientId(), username: String) {
        storeTokenRepository.remove(TokenId(clientId = clientId, username = username))
    }

    @JvmOverloads
    fun removeToken(clientId: String = getClientId(), usernames: List<String>) {
        storeTokenRepository.remove(usernames.map { TokenId(clientId = clientId, username = it) })
    }

    override fun beforeLogin(request: HttpServletRequest, grantType: String, clientId: String, scope: Set<String>) {
        if (userDetailsService is LoginListener) {
            (userDetailsService as LoginListener).beforeLogin(request, grantType, clientId, scope)
        }
    }

    override fun afterLogin(storeToken: StoreToken, request: HttpServletRequest) {
        if (userDetailsService is LoginListener) {
            (userDetailsService as LoginListener).afterLogin(storeToken, request)
        }
    }

    override fun validate(userDetails: UserDetails) {
        if (userDetailsService is UserDetailsValidator) {
            (userDetailsService as UserDetailsValidator).validate(userDetails)
        }
    }

    override fun validate(clientId: String, scope: Set<String>, userDetails: UserDetails): Boolean {
        return if (userDetailsService is NeedKickedOutValidator) {
            (userDetailsService as NeedKickedOutValidator).validate(clientId, scope,
                    userDetails)
        } else {
            val clientDetails = getClientDetails(clientId)
            clientDetails.needKickedOut(scope)
        }
    }

    private fun getClientId(): String {
        return clientDetailsService.getClientId()
    }

    fun getClientDetails(clientId: String): ClientDetails {
        return clientDetailsService.getClientDetails(clientId)
                ?: throw BadCredentialsException("客户端信息不存在")
    }

    companion object {
        private val DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20)
    }
}
