package top.bettercode.summer.test

import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.ErrorPageRegistry
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.util.ClassUtils
import org.springframework.web.filter.OncePerRequestFilter
import top.bettercode.summer.tools.lang.util.StringUtil
import top.bettercode.summer.web.config.SummerWebUtil.okEnable
import top.bettercode.summer.web.properties.SummerWebProperties
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * A Servlet [Filter] that provides an [ErrorPageRegistry] for non-embedded applications
 * (i.e. deployed WAR files). It registers error pages and handles application errors by filtering
 * requests and forwarding to the error pages instead of letting the server handle them. Error pages
 * are a feature of the servlet spec but there is no Java API for registering them in the spec. This
 * filter works around that by accepting error page registrations from Spring Boot's
 * [ErrorPageRegistrar] (any beans of that type in the context will be applied to this
 * server).
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 2.0.0
 */
open class TestErrorPageFilter(private val errorController: BasicErrorController?,
                               private val summerWebProperties: SummerWebProperties?) : Filter, ErrorPageRegistry, Ordered {
    private var global: String? = null
    private val statuses: MutableMap<Int, String> = HashMap()
    private val exceptions: MutableMap<Class<*>, String> = HashMap()
    private val delegate: OncePerRequestFilter = object : OncePerRequestFilter() {
        override fun doFilterInternal(
                request: HttpServletRequest, response: HttpServletResponse,
                chain: FilterChain) {
            this@TestErrorPageFilter.doFilter(request, response, chain)
        }

        override fun shouldNotFilterAsyncDispatch(): Boolean {
            return false
        }
    }

    override fun init(filterConfig: FilterConfig) {
        delegate.init(filterConfig)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        delegate.doFilter(request, response, chain)
    }

    private fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val wrapped = ErrorWrapperResponse(response)
        try {
            chain.doFilter(request, wrapped)
            if (wrapped.hasErrorToSend()) {
                handleErrorStatus(request, response, wrapped.status, wrapped.message)
                response.flushBuffer()
            } else if (!request.isAsyncStarted && !response.isCommitted) {
                response.flushBuffer()
            }
        } catch (ex: Throwable) {
            var exceptionToHandle = ex
            if (ex is ServletException) {
                val rootCause = ex.rootCause
                if (rootCause != null) {
                    exceptionToHandle = rootCause
                }
            }
            handleException(request, response, wrapped, exceptionToHandle)
            response.flushBuffer()
        }
    }

    private fun handleErrorStatus(request: HttpServletRequest, response: HttpServletResponse,
                                  status: Int, message: String?) {
        if (response.isCommitted) {
            handleCommittedResponse(request, null)
            return
        }
        response.status = status
        setErrorAttributes(request, status, message)
        handleError(request, response)
    }

    private fun handleError(request: HttpServletRequest, response: HttpServletResponse) {
        val responseEntity = errorController!!.error(request)
        if (summerWebProperties!!.okEnable(request)) {
            response.status = HttpStatus.OK.value()
        }
        StringUtil.objectMapper().writeValue(response.outputStream, responseEntity.body)
        response.flushBuffer()
    }

    private fun handleException(request: HttpServletRequest, response: HttpServletResponse,
                                wrapped: ErrorWrapperResponse,
                                ex: Throwable) {
        if (response.isCommitted) {
            handleCommittedResponse(request, ex)
            return
        }
        forwardToErrorPage(request, wrapped, ex)
    }

    private fun forwardToErrorPage(request: HttpServletRequest, response: HttpServletResponse,
                                   ex: Throwable) {
        setErrorAttributes(request, 500, ex.message)
        request.setAttribute(ERROR_EXCEPTION, ex)
        request.setAttribute(ERROR_EXCEPTION_TYPE, ex.javaClass)
        response.reset()
        response.status = 500
        handleError(request, response)
        request.removeAttribute(ERROR_EXCEPTION)
        request.removeAttribute(ERROR_EXCEPTION_TYPE)
    }

    /**
     * Return the description for the given request. By default this method will return a description
     * based on the request `servletPath` and `pathInfo`.
     *
     * @param request the source request
     * @return the description
     * @since 1.5.0
     */
    protected fun getDescription(request: HttpServletRequest): String {
        val pathInfo = if (request.pathInfo != null) request.pathInfo else ""
        return "[" + request.servletPath + pathInfo + "]"
    }

    private fun handleCommittedResponse(request: HttpServletRequest, ex: Throwable?) {
        if (isClientAbortException(ex)) {
            return
        }
        val message = ("Cannot forward to error page for request " + getDescription(request)
                + " as the response has already been"
                + " committed. As a result, the response may have the wrong status"
                + " code. If your application is running on WebSphere Application"
                + " Server you may be able to resolve this problem by setting"
                + " com.ibm.ws.webcontainer.invokeFlushAfterService to false")
        if (ex == null) {
            logger.error(message)
        } else {
            // User might see the error page without all the data here but throwing the
            // exception isn't going to help anyone (we'll log it to be on the safe side)
            logger.error(message, ex)
        }
    }

    private fun isClientAbortException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        for (candidate in CLIENT_ABORT_EXCEPTIONS) {
            if (candidate.isInstance(ex)) {
                return true
            }
        }
        return isClientAbortException(ex.cause)
    }

    private fun getErrorPath(map: Map<Int, String>, status: Int): String? {
        return if (map.containsKey(status)) {
            map[status]
        } else global
    }

    private fun getErrorPath(type: Class<*>): String? {
        var t = type
        while (t != Any::class.java) {
            val path = exceptions[t]
            if (path != null) {
                return path
            }
            t = t.superclass
        }
        return global
    }

    private fun setErrorAttributes(request: HttpServletRequest, status: Int, message: String?) {
        request.setAttribute(ERROR_STATUS_CODE, status)
        request.setAttribute(ERROR_MESSAGE, message)
        request.setAttribute(ERROR_REQUEST_URI, request.requestURI)
    }

    private fun rethrow(ex: Throwable) {
        if (ex is RuntimeException) {
            throw ex
        }
        if (ex is Error) {
            throw ex
        }
        if (ex is IOException) {
            throw ex
        }
        if (ex is ServletException) {
            throw ex
        }
        throw IllegalStateException(ex)
    }

    override fun addErrorPages(vararg errorPages: ErrorPage) {
        for (errorPage in errorPages) {
            if (errorPage.isGlobal) {
                global = errorPage.path
            } else if (errorPage.status != null) {
                statuses[errorPage.status.value()] = errorPage.path
            } else {
                exceptions[errorPage.exception] = errorPage.path
            }
        }
    }

    override fun destroy() {}
    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 1
    }

    private class ErrorWrapperResponse(response: HttpServletResponse?) : HttpServletResponseWrapper(response) {
        private var status1 = 0
        var message: String? = null
            private set
        private var hasErrorToSend = false
        override fun sendError(status: Int) {
            sendError(status, "")
        }

        override fun sendError(status: Int, message: String) {
            this.status1 = status
            this.message = message
            hasErrorToSend = true
            // Do not call super because the container may prevent us from handling the
            // error ourselves
        }

        override fun getStatus(): Int {
            return if (hasErrorToSend) {
                status1
            } else super.getStatus()
            // If there was no error we need to trust the wrapped response
        }

        override fun flushBuffer() {
            sendErrorIfNecessary()
            super.flushBuffer()
        }

        private fun sendErrorIfNecessary() {
//            if (hasErrorToSend && !isCommitted) {
//        ((HttpServletResponse) getResponse()).sendError(this.status, this.message);
//            }
        }

        fun hasErrorToSend(): Boolean {
            return hasErrorToSend
        }

        override fun getWriter(): PrintWriter {
            sendErrorIfNecessary()
            return super.getWriter()
        }

        override fun getOutputStream(): ServletOutputStream {
            sendErrorIfNecessary()
            return super.getOutputStream()
        }
    }

    companion object {
        private val logger = LogFactory.getLog(TestErrorPageFilter::class.java)

        // From RequestDispatcher but not referenced to remain compatible with Servlet 2.5
        private const val ERROR_EXCEPTION = "javax.servlet.error.exception"
        private const val ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type"
        private const val ERROR_MESSAGE = "javax.servlet.error.message"

        /**
         * The name of the servlet attribute containing request URI.
         */
        const val ERROR_REQUEST_URI = "javax.servlet.error.request_uri"
        private const val ERROR_STATUS_CODE = "javax.servlet.error.status_code"
        private var CLIENT_ABORT_EXCEPTIONS: Set<Class<*>>

        init {
            val clientAbortExceptions: MutableSet<Class<*>> = HashSet()
            addClassIfPresent(clientAbortExceptions, "org.apache.catalina.connector.ClientAbortException")
            CLIENT_ABORT_EXCEPTIONS = Collections.unmodifiableSet(clientAbortExceptions)
        }

        private fun addClassIfPresent(collection: MutableCollection<Class<*>>, className: String) {
            try {
                collection.add(ClassUtils.forName(className, null))
            } catch (ignored: Throwable) {
            }
        }
    }
}
