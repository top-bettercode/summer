package top.bettercode.summer.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.util.AntPathMatcher
import org.springframework.util.Assert
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.HtmlUtils
import top.bettercode.summer.config.ManagementAuthProperties.Companion.DEFAULT_LOGIN_PAGE
import java.nio.charset.StandardCharsets
import java.util.function.Function
import java.util.regex.Pattern
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ManagementLoginPageGeneratingFilter(
    private val managementAuthProperties: ManagementAuthProperties,
    private val webEndpointProperties: WebEndpointProperties
) : GenericFilterBean() {

    private val loggerAuthKey = "${webEndpointProperties.basePath.trimStart('/')}key"
    private val targetUrlKey = "${webEndpointProperties.basePath.trimStart('/')}targetUrl"

    private var logoutSuccessUrl: String
    private var failureUrl: String
    private var authenticationUrl: String
    private var usernameParameter: String
    private var pwdParameter: String
    private val antPathMatcher = AntPathMatcher()
    private var resolveHiddenInputs =
        Function<HttpServletRequest, Map<String?, String>> { emptyMap() }

    /**
     * Sets a Function used to resolve a Map of the hidden inputs where the key is the name of the
     * input and the value is the value of the input. Typically this is used to resolve the CSRF
     * token.
     *
     * @param resolveHiddenInputs the function to resolve the inputs
     */
    fun setResolveHiddenInputs(
        resolveHiddenInputs: Function<HttpServletRequest, Map<String?, String>>
    ) {
        Assert.notNull(resolveHiddenInputs, "resolveHiddenInputs cannot be null")
        this.resolveHiddenInputs = resolveHiddenInputs
    }

    fun setLogoutSuccessUrl(logoutSuccessUrl: String) {
        this.logoutSuccessUrl = logoutSuccessUrl
    }

    fun setFailureUrl(failureUrl: String) {
        this.failureUrl = failureUrl
    }

    fun setAuthenticationUrl(authenticationUrl: String) {
        this.authenticationUrl = authenticationUrl
    }

    fun setUsernameParameter(usernameParameter: String) {
        this.usernameParameter = usernameParameter
    }

    fun setPwdParameter(pwdParameter: String) {
        this.pwdParameter = pwdParameter
    }

    fun match(uri: String): Boolean {
        for (ignore in managementAuthProperties.ignorePattern) {
            if (antPathMatcher.match("${webEndpointProperties.basePath}$ignore", uri)) {
                return false
            }
        }
        if (antPathMatcher.match("${webEndpointProperties.basePath}/**", uri)) {
            return true
        }
        for (pattern in managementAuthProperties.pattern) {
            if (antPathMatcher.match(pattern, uri)) {
                return true
            }
        }
        return false
    }

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse
        var uri = request.servletPath
        if (match(uri)) {
            val key =
                request.session.getAttribute(loggerAuthKey) ?: request.getHeader(loggerAuthKey)
            if (key == managementAuthProperties.authKey) {
                chain.doFilter(request, response)
            } else {
                val queryString = request.queryString
                if (queryString != null) {
                    uri += "?$queryString"
                }
                request.session.setAttribute(targetUrlKey, uri)
                sendRedirect(request, response, authenticationUrl)
            }
        } else {
            val matcheLoginPage = matches(request, authenticationUrl)
            var errorMsg = "Invalid credentials"
            var loginError = isErrorPage(request)
            if (matcheLoginPage && "POST" == request.method) {
                val username = request.getParameter(usernameParameter)
                val password = request.getParameter(pwdParameter)
                if (username != null && password != null && (username.trim { it <= ' ' }
                            == managementAuthProperties.username) && (password
                            == managementAuthProperties.password)) {
                    request.session.maxInactiveInterval = managementAuthProperties.maxAge
                    request.session.setAttribute(loggerAuthKey, managementAuthProperties.authKey)
                    var url = request.session.getAttribute(targetUrlKey)?.toString()
                        ?: webEndpointProperties.basePath
                    request.session.removeAttribute(targetUrlKey)
                    val hash = request.getParameter("hash")
                    if (!hash.isNullOrBlank()) {
                        url += hash
                    }
                    sendRedirect(request, response, url)
                    return
                }
                errorMsg = "用户名或密码错误"
                loginError = true
            }
            if (matcheLoginPage && "GET" == request.method || loginError || isLogoutSuccess(request)) {
                val loginPageHtml = generateLoginPageHtml(
                    request, loginError,
                    isLogoutSuccess(request), errorMsg
                )
                response.contentType = "text/html;charset=UTF-8"
                response.setContentLength(loginPageHtml.toByteArray(StandardCharsets.UTF_8).size)
                val writer = response.writer
                writer.write(loginPageHtml)
                writer.flush()
                response.flushBuffer()
                return
            }
            chain.doFilter(request, response)
        }
    }

    private fun sendRedirect(
        request: HttpServletRequest, response: HttpServletResponse,
        url: String
    ) {
        val redirectUrl = calculateRedirectUrl(request.contextPath, url)
        if (logger.isTraceEnabled) {
            logger.trace("Redirecting to '$redirectUrl'")
        }
        response.sendRedirect(redirectUrl)
    }

    private fun calculateRedirectUrl(contextPath: String, url: String): String {
        return if (!isAbsoluteUrl(url)) {
            contextPath + url
        } else url
    }

    private fun generateLoginPageHtml(
        request: HttpServletRequest, loginError: Boolean,
        logoutSuccess: Boolean, errorMsg: String
    ): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <meta name="description" content="">
  <meta name="author" content="">
  <title>Please sign in</title>
  <style>
    body {
      padding-top: 40px;
      padding-bottom: 40px;
      background-color: #eee;
    }

    .form-signin {
      max-width: 330px;
      padding: 15px;
      margin: 0 auto;
    }

    .form-signin .form-signin-heading,
    .form-signin .checkbox {
      margin-bottom: 10px;
    }

    .form-signin .checkbox {
      font-weight: 400;
    }

    .form-signin .form-control {
      position: relative;
      box-sizing: border-box;
      height: auto;
      padding: 10px;
      font-size: 16px;
    }

    .form-signin .form-control:focus {
      z-index: 2;
    }

    .form-signin input[type="email"] {
      margin-bottom: -1px;
      border-bottom-right-radius: 0;
      border-bottom-left-radius: 0;
    }

    .form-signin input[type="password"] {
      margin-bottom: 10px;
      border-top-left-radius: 0;
      border-top-right-radius: 0;
    }

    .form-control {
      display: block;
      width: 100%;
      padding: .5rem .75rem;
      font-size: 1rem;
      line-height: 1.25;
      color: #495057;
      background-color: #fff;
      background-image: none;
      background-clip: padding-box;
      border: 1px solid rgba(0, 0, 0, .15);
      border-radius: 0.25rem 0.25rem .25rem .25rem;
      transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
    }

    .form-control::placeholder {
      color: #868e96;
      opacity: 1;
    }

    .btn-primary:hover {
      color: #fff;
      background-color: #0069d9;
      border-color: #0062cc
    }

    .btn-block {
      padding: .5rem 1rem;
      font-size: 1.25rem;
      line-height: 1.5;
      border-radius: .3rem;
      color: #fff;
      background-color: #007bff;
      border-color: #007bff;
      display: block;
      width: 100%;
    }
  </style>
</head>
<body>
<div class="container">
  <form class="form-signin" method="post" action="${request.contextPath}$authenticationUrl">
    <input type="hidden" id="hash" name="hash" value="">
    <h2 class="form-signin-heading">Please sign in</h2>
    ${createError(loginError, errorMsg) + createLogoutSuccess(logoutSuccess)}
    <p>
      <label for="username" class="sr-only"></label>
      <input type="text" id="username" name="$usernameParameter" class="form-control"
             placeholder="Username" required autofocus>
    </p>
    <p>
      <label for="password" class="sr-only"></label>
      <input type="password" id="password" name="$pwdParameter" class="form-control"
             placeholder="Password" required>
    </p>
    ${renderHiddenInputs(request)}
    <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
  </form>
</div>
<script>
  document.getElementById("hash").value = window.location.hash;
</script>
</body>
</html>
            """.trimIndent()
    }

    private fun renderHiddenInputs(request: HttpServletRequest): String {
        val sb = StringBuilder()
        for ((key, value) in resolveHiddenInputs.apply(request)) {
            sb.append("<input name=\"").append(key).append("\" type=\"hidden\" value=\"")
                .append(value).append("\" />\n")
        }
        return sb.toString()
    }

    private fun isLogoutSuccess(request: HttpServletRequest): Boolean {
        return "GET" == request.method && matches(request, logoutSuccessUrl)
    }

    private fun isErrorPage(request: HttpServletRequest): Boolean {
        return "GET" == request.method && matches(request, failureUrl)
    }

    private fun matches(request: HttpServletRequest, url: String?): Boolean {
        if (url == null) {
            return false
        }
        var uri = request.requestURI
        val pathParamIndex = uri.indexOf(';')
        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex)
        }
        if (request.queryString != null) {
            uri += "?" + request.queryString
        }
        return if ("" == request.contextPath) {
            uri == url
        } else uri == request.contextPath + url
    }

    companion object {
        const val ERROR_PARAMETER_NAME = "error"

        fun isAbsoluteUrl(url: String?): Boolean {
            if (url == null) {
                return false
            }
            val absoluteUrl = Pattern.compile(
                "\\A[a-z0-9.+-]+://.*",
                Pattern.CASE_INSENSITIVE
            )
            return absoluteUrl.matcher(url).matches()
        }

        private fun createError(isError: Boolean, message: String): String {
            return if (isError) "<div class=\"alert alert-danger\" role=\"alert\">" + HtmlUtils
                .htmlEscape(message) + "</div>" else ""
        }

        private fun createLogoutSuccess(isLogoutSuccess: Boolean): String {
            return if (isLogoutSuccess) "<div class=\"alert alert-success\" role=\"alert\">You have been signed out</div>" else ""
        }
    }

    init {
        authenticationUrl = "${webEndpointProperties.basePath}$DEFAULT_LOGIN_PAGE"
        logoutSuccessUrl = "${webEndpointProperties.basePath}$DEFAULT_LOGIN_PAGE?logout"
        failureUrl = "${webEndpointProperties.basePath}$DEFAULT_LOGIN_PAGE?$ERROR_PARAMETER_NAME"
        usernameParameter = "username"
        pwdParameter = "password"
    }
}