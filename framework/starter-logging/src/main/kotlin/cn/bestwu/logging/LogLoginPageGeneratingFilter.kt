package cn.bestwu.logging

import org.springframework.util.Assert
import org.springframework.util.DigestUtils
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.HtmlUtils
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import javax.servlet.FilterChain
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.Function
import java.util.regex.Pattern
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class LogLoginPageGeneratingFilter(
        private val logDocAuthProperties: LogDocAuthProperties, private val logViewPath: String) : GenericFilterBean() {
    var loginPageUrl: String
    private var logoutSuccessUrl: String
    private var failureUrl: String
    private var authenticationUrl: String
    private var usernameParameter: String
    private var pwdParameter: String
    private var resolveHiddenInputs = Function<HttpServletRequest, Map<String?, String>> { emptyMap() }
    private val authKey: String = DigestUtils.md5DigestAsHex("${logDocAuthProperties.username}:${logDocAuthProperties.password}".toByteArray())

    /**
     * Sets a Function used to resolve a Map of the hidden inputs where the key is the name of the
     * input and the value is the value of the input. Typically this is used to resolve the CSRF
     * token.
     *
     * @param resolveHiddenInputs the function to resolve the inputs
     */
    fun setResolveHiddenInputs(
            resolveHiddenInputs: Function<HttpServletRequest, Map<String?, String>>) {
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

    fun setPasswordParameter(passwordParameter: String) {
        this.pwdParameter = passwordParameter
    }


    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse
        var uri = request.servletPath
        if (logDocAuthProperties.match(uri)) {
            if (request.getCookie(LOGGER_AUTH_KEY) == authKey) {
                chain.doFilter(request, response)
            } else {
                val queryString = request.queryString
                if (queryString != null) {
                    uri += "?$queryString"
                }
                response.setCookie(TARGET_URL_KEY, uri)
                sendRedirect(request, response, loginPageUrl)
            }
            return
        }
        val matcheLoginPage = matches(request, loginPageUrl)
        var errorMsg = "Invalid credentials"
        var loginError = isErrorPage(request)
        if (matcheLoginPage && "POST" == request.method) {
            val username = request.getParameter(usernameParameter)
            val password = request.getParameter(pwdParameter)
            if (username != null && password != null && (username.trim { it <= ' ' }
                            == logDocAuthProperties.username) && (password
                            == logDocAuthProperties.password)) {
                response.setCookie(LOGGER_AUTH_KEY, authKey)
                val url = request.getCookie(TARGET_URL_KEY) ?: logViewPath
                sendRedirect(request, response, url)
                return
            }
            errorMsg = "用户名或密码错误"
            loginError = true
        }
        if (matcheLoginPage && "GET" == request.method || loginError || isLogoutSuccess(
                        request)) {
            val loginPageHtml = generateLoginPageHtml(request, loginError,
                    isLogoutSuccess(request), errorMsg)
            response.contentType = "text/html;charset=UTF-8"
            response.setContentLength(loginPageHtml.toByteArray(StandardCharsets.UTF_8).size)
            response.writer.write(loginPageHtml)
            return
        }
        chain.doFilter(request, response)
    }

    private fun HttpServletRequest.getCookie(name: String): String? {
        return cookies?.find { it.name == name }?.value
    }

    private fun HttpServletResponse.setCookie(name: String, value: String) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.maxAge = -1
        this.addCookie(cookie)
    }

    private fun sendRedirect(request: HttpServletRequest, response: HttpServletResponse,
                             url: String) {
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

    private fun generateLoginPageHtml(request: HttpServletRequest, loginError: Boolean,
                                      logoutSuccess: Boolean, errorMsg: String): String {
        val sb = StringBuilder()
        sb.append("""<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Please sign in</title>
    <style type="text/css">
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
        border-radius: .25rem;
        border-top-left-radius: 0.25rem;
        border-top-right-radius: 0.25rem;
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
    </style>  </head>
  <body>
     <div class="container">
""")
        val contextPath = request.contextPath
        sb.append("      <form class=\"form-signin\" method=\"post\" action=\"")
                .append(contextPath)
                .append(authenticationUrl)
                .append("\">\n")
                .append("        <h2 class=\"form-signin-heading\">Please sign in</h2>\n")
                .append(createError(loginError, errorMsg))
                .append(createLogoutSuccess(logoutSuccess))
                .append("        <p>\n")
                .append("          <label for=\"username\" class=\"sr-only\"></label>\n")
                .append("          <input type=\"text\" id=\"username\" name=\"")
                .append(usernameParameter)
                .append("\" class=\"form-control\" placeholder=\"Username\" required autofocus>\n")
                .append("        </p>\n")
                .append("        <p>\n")
                .append("          <label for=\"password\" class=\"sr-only\"></label>\n")
                .append("          <input type=\"password\" id=\"password\" name=\"")
                .append(pwdParameter)
                .append("\" class=\"form-control\" placeholder=\"Password\" required>\n")
                .append("        </p>\n")
                .append(renderHiddenInputs(request))
                .append("        <button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Sign in</button>\n")
                .append("      </form>\n")
        sb.append("</div>\n")
        sb.append("</body></html>")
        return sb.toString()
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
        return "GET" == request.method && matches(request,
                logoutSuccessUrl)
    }

    private fun isLoginUrlRequest(request: HttpServletRequest): Boolean {
        return "GET" == request.method && matches(request, loginPageUrl)
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
        const val DEFAULT_LOGIN_PAGE_URL = "/login"
        const val ERROR_PARAMETER_NAME = "error"
        val LOGGER_AUTH_KEY = "_key"
        val TARGET_URL_KEY = "_targetUrl"
        fun isAbsoluteUrl(url: String?): Boolean {
            if (url == null) {
                return false
            }
            val ABSOLUTE_URL = Pattern.compile("\\A[a-z0-9.+-]+://.*",
                    Pattern.CASE_INSENSITIVE)
            return ABSOLUTE_URL.matcher(url).matches()
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
        loginPageUrl = DEFAULT_LOGIN_PAGE_URL
        authenticationUrl = DEFAULT_LOGIN_PAGE_URL
        logoutSuccessUrl = "$DEFAULT_LOGIN_PAGE_URL?logout"
        failureUrl = "$DEFAULT_LOGIN_PAGE_URL?$ERROR_PARAMETER_NAME"
        usernameParameter = "username"
        pwdParameter = "password"
    }
}