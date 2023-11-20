package top.bettercode.summer.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.core.Ordered
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.filter.OncePerRequestFilter
import java.io.File
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * @author Peter Wu
 */
class NavFilter(
        private val webEndpointProperties: WebEndpointProperties,
        private val resourceLoader: ResourceLoader
) : OncePerRequestFilter(), Ordered {
    var resolver = PathMatchingResourcePatternResolver()

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
            val name = STATIC_LOCATIONS + webEndpointProperties.basePath + "/doc/"
            val resource = resourceLoader.getResource(name)
            if (resource.exists()) {
                val url = resource.url.toString()
                if (url.startsWith("jar:")) {
                    val resources = resolver.getResources("$name**").map { it.url.path.substringAfter(resource.url.path) }.filter { it.matches("^v.*\\.html$".toRegex()) }
                    val last = resources
                            .sortedWith { o1, o2 -> o1.compareTo(o2) }.lastOrNull()
                    if (last != null) {
                        response.sendRedirect(
                                request.contextPath + webEndpointProperties.basePath + "/doc/" + last
                        )
                        return
                    }
                } else if (resource.file.exists()) {
                    val last = Arrays.stream(resource.file.listFiles())
                            .filter { f: File -> f.isFile && f.name.matches("^v.*\\.html$".toRegex()) }
                            .max { o1, o2 -> o1.name.compareTo(o2.name) }
                    if (last.isPresent) {
                        response.sendRedirect(
                                request.contextPath + webEndpointProperties.basePath + "/doc/" + last
                                        .get()
                                        .name
                        )
                        return
                    }
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val STATIC_LOCATIONS = "classpath:/META-INF/resources"
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 1
    }
}