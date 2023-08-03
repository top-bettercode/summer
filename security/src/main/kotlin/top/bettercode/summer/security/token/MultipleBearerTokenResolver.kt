package top.bettercode.summer.security.token

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestMethod
import top.bettercode.summer.security.support.SecurityParameterNames
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

class MultipleBearerTokenResolver {
    private val log = LoggerFactory.getLogger(MultipleBearerTokenResolver::class.java)
    private var allowFormEncodedBodyParameter = true
    private var allowUriQueryParameter = true
    private var bearerTokenHeaderName = HttpHeaders.AUTHORIZATION

    /**
     * 是否兼容旧toekn名称
     */
    private var compatibleAccessToken: Boolean? = false
    fun resolve(request: HttpServletRequest): String? {
        var authorizationHeaderToken = resolveFromAuthorizationHeader(request)
        if (authorizationHeaderToken == null && isParameterTokenSupportedForRequest(request)) {
            authorizationHeaderToken = resolveFromRequestParameters(request,
                    SecurityParameterNames.ACCESS_TOKEN)
        }
        if (compatibleAccessToken!!) {
            if (authorizationHeaderToken == null) {
                authorizationHeaderToken = resolveCompatibleAccessTokenFromHeader(request)
            }
            if (authorizationHeaderToken == null && isParameterTokenSupportedForRequest(request)) {
                authorizationHeaderToken = resolveFromRequestParameters(request,
                        SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
            }
        }
        return authorizationHeaderToken
    }

    /**
     * Set if transport of access token using form-encoded body parameter is supported. Defaults to
     * `false`.
     *
     * @param allowFormEncodedBodyParameter if the form-encoded body parameter is supported
     */
    fun setAllowFormEncodedBodyParameter(allowFormEncodedBodyParameter: Boolean) {
        this.allowFormEncodedBodyParameter = allowFormEncodedBodyParameter
    }

    /**
     * Set if transport of access token using URI query parameter is supported. Defaults to
     * `false`.
     *
     *
     * The spec recommends against using this mechanism for sending bearer tokens, and even goes as
     * far as stating that it was only included for completeness.
     *
     * @param allowUriQueryParameter if the URI query parameter is supported
     */
    fun setAllowUriQueryParameter(allowUriQueryParameter: Boolean) {
        this.allowUriQueryParameter = allowUriQueryParameter
    }

    /**
     * Set this value to configure what header is checked when resolving a Bearer Token. This value is
     * defaulted to [HttpHeaders.AUTHORIZATION].
     *
     *
     * This allows other headers to be used as the Bearer Token source such as
     * [HttpHeaders.PROXY_AUTHORIZATION]
     *
     * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
     * @since 5.4
     */
    fun setBearerTokenHeaderName(bearerTokenHeaderName: String) {
        this.bearerTokenHeaderName = bearerTokenHeaderName
    }

    fun setCompatibleAccessToken(compatibleAccessToken: Boolean?) {
        this.compatibleAccessToken = compatibleAccessToken
    }

    private fun resolveFromAuthorizationHeader(request: HttpServletRequest): String? {
        val authorization = request.getHeader(bearerTokenHeaderName)
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null
        }
        val matcher = authorizationPattern.matcher(authorization)
        return if (matcher.matches()) {
            matcher.group("token")
        } else {
            log.warn("Bearer token is malformed")
            null
        }
    }

    private fun resolveCompatibleAccessTokenFromHeader(request: HttpServletRequest): String? {
        return request.getHeader(SecurityParameterNames.COMPATIBLE_ACCESS_TOKEN)
    }

    private fun isParameterTokenSupportedForRequest(request: HttpServletRequest): Boolean {
        val method = request.method
        return ((allowFormEncodedBodyParameter
                && (RequestMethod.POST.name == method || RequestMethod.PUT.name == method))
                || (allowUriQueryParameter
                && (RequestMethod.GET.name == method || RequestMethod.DELETE.name == method)))
    }

    companion object {
        private val authorizationPattern = Pattern
                .compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
                        Pattern.CASE_INSENSITIVE)

        private fun resolveFromRequestParameters(request: HttpServletRequest, tokenName: String): String? {
            val values = request.getParameterValues(tokenName)
            return if (values == null || values.isEmpty()) {
                null
            } else values[0]
        }
    }
}
