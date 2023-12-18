package top.bettercode.summer.security

import jakarta.servlet.http.HttpServletRequest
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
        val tokenValue =
            Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (storeTokenRepository.findByAccessToken(tokenValue) != null) {
            return createAccessToken(clientDetails)
        }
        val now = Instant.now()
        val accessTokenValiditySeconds = clientDetails.accessTokenValiditySeconds
        return Token(
            tokenValue, now,
            if (accessTokenValiditySeconds > 0) now.plusSeconds(
                accessTokenValiditySeconds.toLong()
            ) else null
        )
    }

    private fun createRefreshToken(clientDetails: ClientDetails): Token {
        val tokenValue =
            Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())
        if (storeTokenRepository.findByRefreshToken(tokenValue) != null) {
            return createRefreshToken(clientDetails)
        }
        val now = Instant.now()
        val refreshTokenValiditySeconds = clientDetails.refreshTokenValiditySeconds
        return Token(
            tokenValue, now,
            if (refreshTokenValiditySeconds > 0) now.plusSeconds(
                refreshTokenValiditySeconds.toLong()
            ) else null
        )
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: Set<String>,
        username: String
    ): IAccessToken {
        val userDetails = getUserDetails(clientId, scope, username)
        return getAccessToken(clientId, scope, userDetails, validate(clientId, scope, userDetails))
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: String,
        username: String
    ): IAccessToken {
        return getAccessToken(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: Set<String>,
        username: String,
        loginKickedOut: Boolean
    ): IAccessToken {
        val userDetails = getUserDetails(clientId, scope, username)
        return getAccessToken(clientId, scope, userDetails, loginKickedOut)
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: String,
        username: String,
        loginKickedOut: Boolean
    ): IAccessToken {
        return getAccessToken(clientId, setOf(scope), username, loginKickedOut)
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: Set<String>,
        userDetails: UserDetails,
        loginKickedOut: Boolean = validate(clientId, scope, userDetails)
    ): IAccessToken {
        val storeToken = getStoreToken(clientId, scope, userDetails, loginKickedOut)
        storeTokenRepository.save(storeToken)
        return accessTokenConverter.convert(storeToken)
    }

    @JvmOverloads
    fun getAccessToken(
        clientId: String = defaultClientId,
        scope: String,
        userDetails: UserDetails,
        loginKickedOut: Boolean = validate(clientId, setOf(scope), userDetails)
    ): IAccessToken {
        return getAccessToken(clientId, setOf(scope), userDetails, loginKickedOut)
    }

    @JvmOverloads
    fun refreshUserDetails(
        clientId: String = defaultClientId,
        scope: Set<String>,
        oldUsername: String,
        newUsername: String
    ) {
        val oldUserDetails = getUserDetails(clientId, scope, oldUsername)
        val apiToken = getStoreToken(clientId, scope, oldUserDetails, false)
        apiToken.userDetails = getUserDetails(clientId, scope, newUsername)
        storeTokenRepository.save(apiToken)
    }

    @JvmOverloads
    fun refreshUserDetails(clientId: String = defaultClientId, scope: String, username: String) {
        refreshUserDetails(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun refreshUserDetails(
        clientId: String = defaultClientId,
        scope: Set<String>,
        username: String
    ) {
        val userDetails = getUserDetails(clientId, scope, username)
        getAccessToken(clientId, scope, userDetails)
    }

    @JvmOverloads
    fun getStoreToken(
        clientId: String = defaultClientId,
        scope: String,
        userDetails: UserDetails,
        loginKickedOut: Boolean = validate(clientId, setOf(scope), userDetails)
    ): StoreToken {
        return getStoreToken(clientId, setOf(scope), userDetails, loginKickedOut)
    }


    @JvmOverloads
    fun getStoreToken(
        clientId: String = defaultClientId,
        scope: Set<String>,
        userDetails: UserDetails,
        loginKickedOut: Boolean = validate(clientId, scope, userDetails)
    ): StoreToken {
        val clientDetails = getClientDetails(clientId)
        val tokenId = TokenId(clientId = clientId, scope = scope, username = userDetails.username)

        var storeToken: StoreToken?
        if (loginKickedOut) {
            storeToken = StoreToken(
                id = tokenId,
                clientId = clientId,
                scope = scope,
                accessToken = createAccessToken(clientDetails),
                refreshToken = createRefreshToken(clientDetails),
                userDetails = userDetails
            )
        } else {
            storeToken = storeTokenRepository.findById(tokenId)
            if (storeToken == null || storeToken.refreshToken.isExpired) {
                storeToken = StoreToken(
                    tokenId,
                    clientId = clientId,
                    scope = scope,
                    accessToken = createAccessToken(clientDetails),
                    refreshToken = createRefreshToken(clientDetails),
                    userDetails = userDetails
                )
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
                grantType, request
            )
        }
        throw IllegalArgumentException("不支持的grantType类型")
    }

    @JvmOverloads
    fun getUserDetails(
        clientId: String = defaultClientId,
        scope: String,
        username: String
    ): UserDetails {
        return getUserDetails(clientId, setOf(scope), username)
    }

    @JvmOverloads
    fun getUserDetails(
        clientId: String = defaultClientId,
        scope: Set<String>,
        username: String
    ): UserDetails {
        val userDetails: UserDetails = if (userDetailsService is ClientUserDetailsService) {
            userDetailsService.loadUserByClientAndUsername(clientId, scope, username)
        } else {
            userDetailsService.loadUserByUsername(username)
        }
        return userDetails
    }

    fun removeTokenByScope(scope: String, username: String) {
        storeTokenRepository.remove(
            TokenId(
                clientId = defaultClientId,
                scope = setOf(scope),
                username = username
            )
        )
    }

    fun removeTokenByScope(scope: String, username: List<String>) {
        storeTokenRepository.remove(username.map {
            TokenId(
                clientId = defaultClientId,
                scope = setOf(scope),
                username = it
            )
        })
    }

    @JvmOverloads
    fun removeToken(
        clientId: String = defaultClientId,
        scope: String = defaultScope.first(),
        username: String
    ) {
        storeTokenRepository.remove(
            TokenId(
                clientId = clientId,
                scope = setOf(scope),
                username = username
            )
        )
    }

    @JvmOverloads
    fun removeToken(
        clientId: String = defaultClientId,
        scope: String = defaultScope.first(),
        username: List<String>
    ) {
        storeTokenRepository.remove(username.map {
            TokenId(
                clientId = clientId,
                scope = setOf(scope),
                username = it
            )
        })
    }

    @JvmOverloads
    fun removeTokens(
        clientId: String = defaultClientId,
        scope: Set<String> = defaultScope,
        username: List<String>
    ) {
        storeTokenRepository.remove(username.map {
            TokenId(
                clientId = clientId,
                scope = scope,
                username = it
            )
        })
    }

    override fun beforeLogin(
        request: HttpServletRequest,
        grantType: String,
        clientId: String,
        scope: Set<String>
    ) {
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
            (userDetailsService as NeedKickedOutValidator).validate(
                clientId, scope,
                userDetails
            )
        } else {
            val clientDetails = getClientDetails(clientId)
            clientDetails.needKickedOut(scope)
        }
    }

    private val defaultClientId: String
        get() {
            return clientDetailsService.getClientId()
        }

    private val defaultScope: Set<String>
        get() {
            return securityProperties.scope
        }

    fun getClientDetails(clientId: String): ClientDetails {
        return clientDetailsService.getClientDetails(clientId)
            ?: throw BadCredentialsException("客户端信息不存在")
    }

    companion object {
        private val DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(20)
    }
}
