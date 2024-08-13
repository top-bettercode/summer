package top.bettercode.summer.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.Assert
import org.springframework.web.filter.OncePerRequestFilter
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken
import top.bettercode.summer.security.authorize.ClientAuthorize
import top.bettercode.summer.security.repository.StoreTokenRepository
import top.bettercode.summer.security.support.AuthenticationHelper
import top.bettercode.summer.security.support.SecurityParameterNames
import top.bettercode.summer.security.token.IRevokeTokenService
import top.bettercode.summer.security.token.MultipleBearerTokenResolver
import top.bettercode.summer.security.token.StoreToken
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.RespEntity.Companion.ok
import top.bettercode.summer.web.config.SummerWebUtil.okEnable
import top.bettercode.summer.web.config.SummerWebUtil.wrapEnable
import top.bettercode.summer.web.exception.UnauthorizedException
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder.getHandler
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiTokenEndpointFilter @JvmOverloads constructor(
    private val apiTokenService: ApiTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val summerWebProperties: SummerWebProperties,
    private val revokeTokenService: IRevokeTokenService?,
    private val objectMapper: ObjectMapper,
    private val formkeyService: IFormkeyService,
    tokenEndpointUri: String = DEFAULT_TOKEN_ENDPOINT_URI,
    revokeTokenEndpointUri: String = DEFAULT_REVOKE_TOKEN_ENDPOINT_URI
) : OncePerRequestFilter() {

    companion object {
        /**
         * The default endpoint `URI` for access token requests.
         */
        private const val DEFAULT_TOKEN_ENDPOINT_URI = "/oauth/token"
        private const val DEFAULT_REVOKE_TOKEN_ENDPOINT_URI = "/oauth/revokeToken"
        fun needClientAuthorize(request: HttpServletRequest?): Boolean {
            //ClientAuthorize
            val handler = getHandler(request!!)
            return if (handler != null) {
                hasAnnotation(handler, ClientAuthorize::class.java)
            } else false
        }
    }


    private val tokenEndpointMatcher: RequestMatcher
    private val revokeTokenEndpointMatcher: RequestMatcher

    private val bearerTokenResolver = MultipleBearerTokenResolver()

    init {
        Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty")
        tokenEndpointMatcher = AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name)
        revokeTokenEndpointMatcher = OrRequestMatcher(
            listOf(
                AntPathRequestMatcher(tokenEndpointUri, HttpMethod.DELETE.name),
                AntPathRequestMatcher(revokeTokenEndpointUri, HttpMethod.GET.name)
            )
        )
        bearerTokenResolver.setCompatibleAccessToken(apiTokenService.securityProperties.isCompatibleAccessToken)
    }


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val storeTokenRepository = apiTokenService.storeTokenRepository
        //token相关接口
        if (tokenEndpointMatcher.matches(request)) {
            try {
                formkeyService.duplicateCheck(
                    request = request,
                    formKeyName = summerWebProperties.formKeyName,
                ) {
                    val clientId = authenticateBasic(request)
                    val grantType = request.getParameter(SecurityParameterNames.GRANT_TYPE)
                    Assert.hasText(grantType, "grantType 不能为空")

                    val clientDetails =
                        apiTokenService.clientDetailsService.getClientDetails(clientId)
                            ?: throw BadCredentialsException("客户端信息不存在")

                    val storeToken: StoreToken?
                    if (SecurityParameterNames.REVOKE_TOKEN == grantType) {
                        val revokeToken = request.getParameter(SecurityParameterNames.REVOKE_TOKEN)
                        Assert.hasText(
                            revokeToken,
                            SecurityParameterNames.REVOKE_TOKEN + "不能为空"
                        )
                        storeToken = storeTokenRepository.findByAccessToken(revokeToken)
                        if (storeToken == null || storeToken.refreshToken.isExpired) {
                            if (storeToken != null) {
                                storeTokenRepository.remove(storeToken)
                            }
                            throw UnauthorizedException("请重新登录")
                        }
                        try {
                            val userDetails = apiTokenService.getUserDetails(
                                storeToken.clientId,
                                storeToken.scope,
                                storeToken.username
                            )
                            revokeToken(
                                userDetails,
                                storeTokenRepository,
                                storeToken,
                                response,
                                request
                            )
                        } catch (e: Exception) {
                            throw UnauthorizedException(e.message, e)
                        }
                    } else {
                        val scope = request.getParameterValues(SecurityParameterNames.SCOPE)
                            ?.toSet() ?: emptySet()
//                    Assert.isTrue(scope.isNotEmpty(), "scope 不能为空")
                        Assert.isTrue(clientDetails.supportScope(scope), "不支持的scope:$scope")

                        if (SecurityParameterNames.PWDNAME == grantType) {
                            apiTokenService.beforeLogin(request, grantType, clientId, scope)
                            val username = request.getParameter(SecurityParameterNames.USERNAME)
                            Assert.hasText(username, "用户名不能为空")
                            val password = request.getParameter(SecurityParameterNames.PWDNAME)
                            Assert.hasText(password, "密码不能为空")
                            val userDetails =
                                apiTokenService.getUserDetails(clientId, scope, username)
                            Assert.isTrue(
                                passwordEncoder.matches(password, userDetails.password),
                                "用户名或密码错误"
                            )

                            storeToken = apiTokenService.getStoreToken(clientId, scope, userDetails)
                            apiTokenService.afterLogin(storeToken, request)
                        } else if (SecurityParameterNames.REFRESH_TOKEN == grantType) {
                            val refreshToken =
                                request.getParameter(SecurityParameterNames.REFRESH_TOKEN)
                            Assert.hasText(
                                refreshToken,
                                SecurityParameterNames.REFRESH_TOKEN + "不能为空"
                            )
                            storeToken = storeTokenRepository.findByRefreshToken(
                                refreshToken
                            )
                            if (storeToken == null || storeToken.refreshToken.isExpired) {
                                if (storeToken != null) {
                                    storeTokenRepository.remove(storeToken)
                                }
                                throw UnauthorizedException("请重新登录")
                            }
                            try {
                                val userDetails = apiTokenService.getUserDetails(
                                    clientId,
                                    scope,
                                    storeToken.username
                                )
                                storeToken.accessToken =
                                    apiTokenService.createAccessToken(clientDetails)
                                storeToken.userDetails = userDetails
                            } catch (e: Exception) {
                                throw UnauthorizedException(e.message, e)
                            }
                        } else if (apiTokenService.securityProperties.authorizedGrantTypes.contains(
                                grantType
                            )
                        ) {
                            val userDetails = apiTokenService.getUserDetails(grantType, request)
                            storeToken = apiTokenService.getStoreToken(clientId, scope, userDetails)
                        } else {
                            throw IllegalArgumentException("grantType 不支持:$grantType")
                        }
                        val userDetails = storeToken.userDetails
                        val authenticationResult: Authentication =
                            UserDetailsAuthenticationToken(storeToken.id, userDetails)
                        storeTokenRepository.save(storeToken)
                        val context = SecurityContextHolder.createEmptyContext()
                        context.authentication = authenticationResult
                        SecurityContextHolder.setContext(context)
                        response.setHeader(
                            HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE
                        )
                        var apiTokenResponse: Any =
                            apiTokenService.accessTokenConverter.convert(storeToken)
                        if (summerWebProperties.wrapEnable(request)) {
                            apiTokenResponse = ok(apiTokenResponse)
                        }
                        objectMapper.writeValue(response.outputStream, apiTokenResponse)
                    }
                }
            } catch (ex: Exception) {
                SecurityContextHolder.clearContext()
                throw ex
            }
        } else {
            //授权验证
            val accessToken = bearerTokenResolver.resolve(request)
            if (!accessToken.isNullOrBlank()) {
                val storeToken = storeTokenRepository.findByAccessToken(accessToken)
                if (storeToken == null) {
                    if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                        throw UnauthorizedException("错误或过期的token:$accessToken")
                    }
                    logger.warn("错误或过期的token:$accessToken")
                } else {
                    val clientId = storeToken.clientId
                    val clientDetails = apiTokenService.getClientDetails(clientId)
                    val scope = storeToken.scope
                    val supportScope = clientDetails.supportScope(scope)
                    if (!storeToken.accessToken.isExpired && supportScope) {
                        try {
                            val userDetails = storeToken.userDetails
                            apiTokenService.validate(userDetails)
                            val authenticationResult: Authentication =
                                UserDetailsAuthenticationToken(storeToken.id, userDetails)
                            val context = SecurityContextHolder.createEmptyContext()
                            context.authentication = authenticationResult
                            request.setAttribute(
                                HttpOperation.REQUEST_LOGGING_USERNAME,
                                storeToken.id.toString()
                            )
                            SecurityContextHolder.setContext(context)
                            if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                                revokeToken(
                                    userDetails,
                                    storeTokenRepository,
                                    storeToken,
                                    response,
                                    request
                                )
                                return
                            }
                        } catch (failed: Exception) {
                            SecurityContextHolder.clearContext()
                            storeTokenRepository.remove(storeToken)
                            throw failed
                        }
                    } else {
                        if (!supportScope) {
                            if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                                throw UnauthorizedException("不支持token所属scope:$scope")
                            }
                            logger.warn("不支持token scope:$scope")
                        } else if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                            throw UnauthorizedException("错误或过期的token:$accessToken")
                        } else {
                            logger.warn("错误或过期的token:$accessToken")
                        }
                    }
                }
            } else if (needClientAuthorize(request)) {
                authenticateBasic(request)
            }
            filterChain.doFilter(request, response)
        }
    }

    private fun revokeToken(
        userDetails: UserDetails,
        storeTokenRepository: StoreTokenRepository,
        storeToken: StoreToken,
        response: HttpServletResponse,
        request: HttpServletRequest
    ) {
        revokeTokenService?.revokeToken(userDetails)
        storeTokenRepository.remove(storeToken)
        SecurityContextHolder.clearContext()
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        if (summerWebProperties.okEnable(request)) {
            response.status = HttpStatus.OK.value()
        } else {
            response.status = HttpStatus.NO_CONTENT.value()
        }
        if (summerWebProperties.wrapEnable(request)) {
            val respEntity = RespEntity<Any>()
            respEntity.status = HttpStatus.NO_CONTENT.value().toString()
            objectMapper.writeValue(response.outputStream, respEntity)
        } else {
            response.flushBuffer()
        }
    }

    private fun authenticateBasic(request: HttpServletRequest): String {
        val (clientId, clientSecret) = AuthenticationHelper.getClientInfo(request)
        apiTokenService.clientDetailsService.authenticate(
            clientId
                ?: throw BadCredentialsException("Unauthorized"), clientSecret
                ?: throw BadCredentialsException("Unauthorized")
        )
        return clientId
    }

}
