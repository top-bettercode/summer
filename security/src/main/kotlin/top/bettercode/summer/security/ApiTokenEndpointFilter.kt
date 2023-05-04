package top.bettercode.summer.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import top.bettercode.summer.security.authorization.UserDetailsAuthenticationToken
import top.bettercode.summer.security.authorize.ClientAuthorize
import top.bettercode.summer.security.support.SecurityParameterNames
import top.bettercode.summer.security.token.ApiToken
import top.bettercode.summer.security.token.IRevokeTokenService
import top.bettercode.summer.security.token.MultipleBearerTokenResolver
import top.bettercode.summer.tools.lang.operation.HttpOperation
import top.bettercode.summer.tools.lang.util.AnnotatedUtils.hasAnnotation
import top.bettercode.summer.web.RespEntity
import top.bettercode.summer.web.RespEntity.Companion.ok
import top.bettercode.summer.web.exception.UnauthorizedException
import top.bettercode.summer.web.form.FormDuplicateCheckInterceptor
import top.bettercode.summer.web.form.IFormkeyService
import top.bettercode.summer.web.properties.SummerWebProperties
import top.bettercode.summer.web.servlet.HandlerMethodContextHolder.getHandler
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ApiTokenEndpointFilter @JvmOverloads constructor(
        private val apiTokenService: ApiTokenService,
        private val passwordEncoder: PasswordEncoder,
        private val summerWebProperties: SummerWebProperties,
        revokeTokenService: IRevokeTokenService?,
        objectMapper: ObjectMapper,
        formkeyService: IFormkeyService,
        tokenEndpointUri: String? = DEFAULT_TOKEN_ENDPOINT_URI,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(ApiTokenEndpointFilter::class.java)
    private val tokenEndpointMatcher: RequestMatcher
    private val revokeTokenEndpointMatcher: RequestMatcher
    private val revokeTokenService: IRevokeTokenService?
    private val objectMapper: ObjectMapper
    private val formkeyService: IFormkeyService
    private var basicCredentials: String? = null
    private val bearerTokenResolver = MultipleBearerTokenResolver()

    init {
        val securityProperties = apiTokenService.securityProperties
        basicCredentials = if (StringUtils.hasText(securityProperties.clientId) && StringUtils.hasText(
                        securityProperties.clientSecret)) {
            securityProperties.clientId + ":" + securityProperties.clientSecret
        } else {
            null
        }
        this.revokeTokenService = revokeTokenService
        this.objectMapper = objectMapper
        this.formkeyService = formkeyService
        Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty")
        tokenEndpointMatcher = AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name)
        revokeTokenEndpointMatcher = AntPathRequestMatcher(tokenEndpointUri,
                HttpMethod.DELETE.name)
        bearerTokenResolver.setCompatibleAccessToken(securityProperties.isCompatibleAccessToken)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
    ) {
        val apiTokenRepository = apiTokenService.apiTokenRepository
        val securityProperties = apiTokenService.securityProperties
        if (tokenEndpointMatcher.matches(request)) {
            formkeyService.checkRequest(request, summerWebProperties.formKeyName, true, -1, FormDuplicateCheckInterceptor.DEFAULT_MESSAGE)
            try {
                authenticateBasic(request)
                val grantType = request.getParameter(SecurityParameterNames.Companion.GRANT_TYPE)
                Assert.hasText(grantType, "grantType 不能为空")
                val scope = request.getParameter(SecurityParameterNames.Companion.SCOPE)
                Assert.hasText(scope, "scope 不能为空")
                Assert.isTrue(securityProperties.supportScopes.contains(scope), "不支持的scope:$scope")
                val apiToken: ApiToken?
                if (SecurityParameterNames.Companion.PWDNAME == grantType) {
                    apiTokenService.beforeLogin(request, grantType, scope)
                    val username = request.getParameter(SecurityParameterNames.Companion.USERNAME)
                    Assert.hasText(username, "用户名不能为空")
                    val password = request.getParameter(SecurityParameterNames.Companion.PASSWORD)
                    Assert.hasText(password, "密码不能为空")
                    val userDetails = apiTokenService.getUserDetails(scope, username)
                    Assert.isTrue(passwordEncoder.matches(password, userDetails.password),
                            "用户名或密码错误")
                    apiToken = apiTokenService.getApiToken(scope, userDetails)
                    apiTokenService.afterLogin(apiToken, request)
                } else if (SecurityParameterNames.Companion.REFRESH_TOKEN == grantType) {
                    val refreshToken = request.getParameter(SecurityParameterNames.Companion.REFRESH_TOKEN)
                    Assert.hasText(refreshToken, "refreshToken不能为空")
                    apiToken = apiTokenRepository.findByRefreshToken(
                            refreshToken)
                    if (apiToken == null || apiToken.refreshToken.isExpired) {
                        if (apiToken != null) {
                            apiTokenRepository.remove(apiToken)
                        }
                        throw UnauthorizedException("请重新登录")
                    }
                    try {
                        val userDetails = apiTokenService.getUserDetails(scope,
                                apiToken.username)
                        apiToken.accessToken = apiTokenService.createAccessToken()
                        apiToken.userDetailsInstantAt = apiTokenService.createUserDetailsInstantAt()
                        apiToken.userDetails = userDetails
                    } catch (e: Exception) {
                        throw UnauthorizedException("请重新登录", e)
                    }
                } else {
                    val userDetails = apiTokenService.getUserDetails(grantType, request)
                    apiToken = apiTokenService.getApiToken(scope, userDetails)
                }
                val userDetails = apiToken.userDetails
                val authenticationResult: Authentication = UserDetailsAuthenticationToken(userDetails)
                apiTokenRepository.save(apiToken)
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = authenticationResult
                SecurityContextHolder.setContext(context)
                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                var apiTokenResponse: Any? = apiToken.toApiToken()
                if (summerWebProperties.wrapEnable(request)) {
                    apiTokenResponse = ok(apiTokenResponse)
                }
                objectMapper.writeValue(response.outputStream, apiTokenResponse)
            } catch (ex: Exception) {
                formkeyService.cleanKey(request)
                SecurityContextHolder.clearContext()
                throw ex
            }
        } else {
            val accessToken = bearerTokenResolver.resolve(request)
            if (StringUtils.hasText(accessToken)) {
                val apiToken = apiTokenRepository.findByAccessToken(accessToken)
                if (apiToken != null && !apiToken.accessToken.isExpired && securityProperties.supportScopes.contains(apiToken.scope)) {
                    try {
                        val scope = apiToken.scope
                        var userDetails = apiToken.userDetails
                        apiTokenService.validate(userDetails)
                        if (apiToken.userDetailsInstantAt.isExpired) { //刷新userDetails
                            userDetails = apiTokenService.getUserDetails(scope, apiToken.username)
                            apiToken.userDetailsInstantAt = apiTokenService.createUserDetailsInstantAt()
                            apiToken.userDetails = userDetails
                            apiTokenRepository.save(apiToken)
                        }
                        val authenticationResult: Authentication = UserDetailsAuthenticationToken(userDetails)
                        val context = SecurityContextHolder.createEmptyContext()
                        context.authentication = authenticationResult
                        val username = userDetails.username
                        request.setAttribute(HttpOperation.REQUEST_LOGGING_USERNAME, "$scope:$username")
                        SecurityContextHolder.setContext(context)
                        if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                            revokeTokenService?.revokeToken(userDetails)
                            apiTokenRepository.remove(apiToken)
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
                            return
                        }
                    } catch (failed: Exception) {
                        SecurityContextHolder.clearContext()
                        apiTokenRepository.remove(apiToken)
                        throw failed
                    }
                } else {
                    if (apiToken != null) {
                        val scope = apiToken.scope
                        if (!securityProperties.supportScopes.contains(scope!!)) {
                            logger.warn("不支持token所属scope:$scope")
                        }
                    }
                    if (revokeTokenEndpointMatcher.matches(request)) { //撤消token
                        throw UnauthorizedException("错误或过期的token:$accessToken")
                    }
                    logger.warn("错误或过期的token:$accessToken")
                }
            } else if (needClientAuthorize(request)) {
                authenticateBasic(request)
            }
            filterChain.doFilter(request, response)
        }
    }

    private fun authenticateBasic(request: HttpServletRequest) {
        if (basicCredentials == null) {
            return
        }
        var header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null) {
            header = header.trim { it <= ' ' }
            if (StringUtils.startsWithIgnoreCase(header, "Basic") && !header.equals("Basic", ignoreCase = true)) {
                val encodedBasicCredentials = String(
                        decode(header.substring(6).toByteArray(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                if (basicCredentials == encodedBasicCredentials) {
                    return
                }
            }
        }
        throw BadCredentialsException("basic authentication 认证失败")
    }

    private fun decode(base64Token: ByteArray): ByteArray {
        return try {
            Base64.getDecoder().decode(base64Token)
        } catch (var3: IllegalArgumentException) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        }
    }

    companion object {
        /**
         * The default endpoint `URI` for access token requests.
         */
        private const val DEFAULT_TOKEN_ENDPOINT_URI = "/oauth/token"
        fun needClientAuthorize(request: HttpServletRequest?): Boolean {
            //ClientAuthorize
            val handler = getHandler(request!!)
            return if (handler != null) {
                hasAnnotation(handler, ClientAuthorize::class.java)
            } else false
        }
    }
}
