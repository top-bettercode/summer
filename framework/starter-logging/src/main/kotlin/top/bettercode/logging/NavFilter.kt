package top.bettercode.logging

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.core.io.ResourceLoader
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import top.bettercode.logging.NavFilter
import java.io.File
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
class NavFilter(
    private val webEndpointProperties: WebEndpointProperties,
    private val resourceLoader: ResourceLoader
) : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val servletPath = request.servletPath
        if (servletPath.startsWith("/doc")) {
            response.sendRedirect(
                request.contextPath + webEndpointProperties.basePath + "/doc" + servletPath
                    .substring(4)
            )
            return
        }
        if (servletPath.startsWith("/logs")) {
            response.sendRedirect(
                request.contextPath + webEndpointProperties.basePath + "/logs" + servletPath
                    .substring(5)
            )
            return
        }
        if (webEndpointProperties.basePath + "/doc" == servletPath || webEndpointProperties.basePath + "/doc/" == servletPath) {
            val name = staticLocations + webEndpointProperties.basePath + "/doc/"
            val resource = resourceLoader.getResource(name)
            if (resource.exists()) {
                val dic = resource.file
                if (dic.exists()) {
                    val file = File(dic, "v1.0.html")
                    if (file.exists()) {
                        response.sendRedirect(
                            request.contextPath + webEndpointProperties.basePath + "/doc/v1.0.html"
                        )
                        return
                    } else {
                        val first = Arrays.stream(dic.listFiles())
                            .filter { f: File -> f.isFile && f.name.endsWith(".html") }
                            .min { o1, o2 -> o1.name.compareTo(o2.name) }
                        if (first.isPresent) {
                            response.sendRedirect(
                                request.contextPath + webEndpointProperties.basePath + "/doc/" + first
                                    .get()
                                    .name
                            )
                            return
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val staticLocations = "classpath:/META-INF/resources"
    }
}