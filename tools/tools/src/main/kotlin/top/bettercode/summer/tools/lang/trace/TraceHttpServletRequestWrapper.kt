package top.bettercode.summer.tools.lang.trace

import org.springframework.http.MediaType
import org.springframework.util.StreamUtils
import top.bettercode.summer.tools.lang.operation.RequestConverter
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.Part

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class TraceHttpServletRequestWrapper

/**
 * Constructs a request object wrapping the given request.
 *
 * @param request The request to wrap
 * @throws IllegalArgumentException if the request is null
 */(val request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private var servletInputStream: TraceServletInputStream? = null
    private var servletReader: BufferedReader? = null
    private var servletParts: MutableCollection<Part>? = null

    val contentAsByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()


    val content: String
        get() = RequestConverter.toString(
                if (!contentType.isNullOrBlank()) MediaType.parseMediaType(
                        contentType
                ).charset else null, byteArrayOutputStream.toByteArray()
        )

    fun read() {
        try {
            if (servletInputStream == null) {
                val inputStream = inputStream
                if (!inputStream.isFinished) {
                    StreamUtils.copyToByteArray(inputStream)
                    servletInputStream!!.reset()
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun getPart(name: String?): Part? {
        return parts.find { it.name == name }
    }

    override fun getParts(): MutableCollection<Part> {
        if (servletParts == null) {
            servletParts = super.getParts().map { TracePart(it) }.toMutableList()
        }
        return servletParts!!
    }

    override fun getInputStream(): ServletInputStream {
        if (servletInputStream == null) {
            servletInputStream =
                    TraceServletInputStream(super.getInputStream(), byteArrayOutputStream)
        }
        return servletInputStream!!
    }

    override fun getReader(): BufferedReader {
        if (servletReader == null) {
            servletReader = BufferedReader(InputStreamReader(this.inputStream))
        }
        return servletReader!!
    }

    override fun getCharacterEncoding(): String {
        return super.getCharacterEncoding() ?: "UTF-8"
    }
}
