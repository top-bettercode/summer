package top.bettercode.summer.data.jpa.support

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ResourceLoader
import org.springframework.lang.Nullable
import org.springframework.web.servlet.HandlerMapping
import top.bettercode.summer.config.DocsEndpoint.Companion.handleRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * @author Peter Wu
 */
@Endpoint(id = "data")
open class DataEndpoint(
    private val request: HttpServletRequest,
    private val response: HttpServletResponse,
    private val resourceLoader: ResourceLoader,
    private val dataQuery: DataQuery
) {
    private val sqlLog = LoggerFactory.getLogger("top.bettercode.summer.SQL")
    private val fileClassPath = "classpath:/META-INF/actuator/data"

    @WriteOperation
    open fun write(
        @Selector ds: String, @Selector op: String, sql: String,
        @Nullable page: Int?,
        @Nullable size: Int?
    ): Any {
        if (sql.isBlank()) {
            return mapOf("error" to "sql不能为空")
        }
        return when (op) {
            "update" -> {
                dataQuery.update(ds, sql.trim().trimEnd(';'))
            }

            "query" -> {
                dataQuery.query(ds, sql.trim().trimEnd(';'), page ?: 1, size ?: 10)
            }

            else -> {
                ""
            }
        }
    }

    @ReadOperation
    open fun read(@Selector(match = Selector.Match.ALL_REMAINING) path: String) {
        val requestPath = path.replace(",", "/").trimEnd('/')
        val isRoot = requestPath.isBlank()
        val docPath = "$fileClassPath/$requestPath"
        val resource = resourceLoader.getResource(docPath)
        val servletPath =
            (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String?
                ?: throw IllegalStateException(
                    "Required request attribute '" +
                            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set"
                )).trimEnd('/')

        if (isRoot) {
            if (resource.exists()) {
                response.sendRedirect("$servletPath/query.html")
            }
        }
        handleRequest(request, response, resource) {
            val urlPath = it.url.path
            if (urlPath.endsWith(".html")) {
                val text = it.inputStream.reader().readText()
                    .replace("@ds@", dataQuery.getDs().joinToString("\n") { ds ->
                        "<option value=\"$ds\">$ds</option>"
                    })
                    .replace("@path@", servletPath.substringBeforeLast("/"))
                object : ByteArrayResource(text.toByteArray()) {
                    override fun getFilename(): String? {
                        return resource.filename
                    }
                }
            } else {
                it
            }
        }

    }


}