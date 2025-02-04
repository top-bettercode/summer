package top.bettercode.summer.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.http.*
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.http.converter.ResourceRegionHttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.lang.Nullable
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.resource.HttpResource
import top.bettercode.summer.logging.LoggingUtil
import top.bettercode.summer.tools.lang.util.StringUtil
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Peter Wu
 */
@Endpoint(id = "doc")
class DocsEndpoint(
    private val request: HttpServletRequest,
    private val response: HttpServletResponse,
    private val resourceLoader: ResourceLoader
) {

    private val fileClassPath = "classpath:/META-INF/actuator/doc"

    @ReadOperation
    fun root(@Selector(match = Selector.Match.ALL_REMAINING) path: String) {
        val requestPath = path.replace(",", "/").trimEnd('/')
        val isRoot = requestPath.isBlank()
        val docPath = "$fileClassPath/$requestPath"
        val resource = resourceLoader.getResource(docPath)
        if (isRoot) {
            val servletPath =
                (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String?
                    ?: throw IllegalStateException(
                        "Required request attribute '" +
                                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set"
                    )).trimEnd('/')

            if (resource.exists()) {
                val url = resource.url.toString()
                if (url.startsWith("jar:")) {
                    val resources = resolver.getResources("$docPath**")
                        .map { it.url.path.substringAfter(resource.url.path) }
                        .filter { it.matches("^v.*\\.html$".toRegex()) }

                    val last = resources
                        .sortedWith { o1, o2 -> StringUtil.compareVersion(o1, o2) }
                        .lastOrNull()
                    if (last != null) {
                        response.sendRedirect("$servletPath/$last")
                        return
                    }
                } else if (resource.file.exists()) {
                    val last = resource.file.listFiles()
                        ?.filter { f: File -> f.isFile && f.name.matches("^v.*\\.html$".toRegex()) }
                        ?.map { it.name }
                        ?.sortedWith { o1, o2 -> StringUtil.compareVersion(o1, o2) }
                        ?.lastOrNull()
                    if (last != null) {
                        response.sendRedirect("$servletPath/${last}")
                        return
                    }
                }
            }
        }
        handleRequest(request, response, resource) {
            val urlPath = it.url.path
            if (urlPath.endsWith(".html") || urlPath.endsWith(".postman_collection.json")) {
                val apiAddress = LoggingUtil.apiAddress.first
                val text = it.inputStream.reader().readText()
                    .replace("\${apiAddress}", apiAddress)
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

    companion object {

        private val log: Logger = LoggerFactory.getLogger(DocsEndpoint::class.java)
        private val mediaTypes: Map<String, MediaType> = HashMap(4)
        private val resourceHttpMessageConverter = ResourceHttpMessageConverter()
        private val resourceRegionHttpMessageConverter = ResourceRegionHttpMessageConverter()
        private val resolver = PathMatchingResourcePatternResolver()

        fun handleRequest(
            request: HttpServletRequest,
            response: HttpServletResponse,
            resource: Resource?,
            convert: (Resource) -> Resource
        ) {
            // For very general mappings (e.g. "/") we need to check 404 first
            if (resource == null || !resource.exists()) {
                if (log.isDebugEnabled)
                    log.debug("Resource not found")
                response.sendError(HttpServletResponse.SC_NOT_FOUND)
                return
            }
            if (HttpMethod.OPTIONS.matches(request.method)) {
                response.setHeader("Allow", "*")
                return
            }

            val responseResource = convert(resource)
            // Check the media type for the resource
            val mediaType = getMediaType(request, responseResource)
            setHeaders(response, responseResource, mediaType)

            // Content phase
            val outputMessage = ServletServerHttpResponse(response)
            if (request.getHeader(HttpHeaders.RANGE) == null) {
                this.resourceHttpMessageConverter.write(responseResource, mediaType, outputMessage)
            } else {
                val inputMessage = ServletServerHttpRequest(request)
                try {
                    val httpRanges = inputMessage.headers.range
                    response.status = HttpServletResponse.SC_PARTIAL_CONTENT
                    this.resourceRegionHttpMessageConverter.write(
                        HttpRange.toResourceRegions(httpRanges, responseResource),
                        mediaType,
                        outputMessage
                    )
                } catch (ex: IllegalArgumentException) {
                    response.setHeader(
                        HttpHeaders.CONTENT_RANGE,
                        "bytes */" + responseResource.contentLength()
                    )
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE)
                }
            }
        }

        private fun getMediaType(request: HttpServletRequest, resource: Resource): MediaType? {
            var result: MediaType? = null
            val mimeType = request.servletContext.getMimeType(resource.filename)
            if (!mimeType.isNullOrBlank()) {
                result = MediaType.parseMediaType(mimeType)
            }
            if (result == null || MediaType.APPLICATION_OCTET_STREAM == result) {
                var mediaType: MediaType? = null
                val filename = resource.filename
                val ext = StringUtils.getFilenameExtension(filename)
                if (ext != null) {
                    mediaType = this.mediaTypes[ext.lowercase()]
                }
                if (mediaType == null) {
                    val mediaTypes = MediaTypeFactory.getMediaTypes(filename)
                    if (!CollectionUtils.isEmpty(mediaTypes)) {
                        mediaType = mediaTypes[0]
                    }
                }
                if (mediaType != null) {
                    result = mediaType
                }
            }
            return result
        }

        private fun setHeaders(
            response: HttpServletResponse,
            resource: Resource?,
            @Nullable mediaType: MediaType?
        ) {
            if (mediaType != null) {
                response.contentType = mediaType.toString()
            }
            if (resource is HttpResource) {
                val resourceHeaders = resource.responseHeaders
                resourceHeaders.forEach { headerName: String?, headerValues: List<String?> ->
                    var first = true
                    for (headerValue in headerValues) {
                        if (first) {
                            response.setHeader(headerName, headerValue)
                        } else {
                            response.addHeader(headerName, headerValue)
                        }
                        first = false
                    }
                }
            }
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes")
        }

    }


}