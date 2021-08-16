package top.bettercode.logging.trace

import org.springframework.web.util.WebUtils
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
 */
constructor(val request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private var servletInputStream: ServletInputStream? = null

    val contentAsByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()


    override fun getPart(name: String?): Part? {
        return parts.find { it.name == name }
    }

    override fun getParts(): MutableCollection<Part> {
        return super.getParts().map { TracePart(it) }.toMutableList()
    }

    override fun getInputStream(): ServletInputStream {
        if (servletInputStream == null) {
            servletInputStream =
                TraceServletInputStream(super.getInputStream(), byteArrayOutputStream)
        }
        return servletInputStream!!
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
    }

    override fun getCharacterEncoding(): String {
        return super.getCharacterEncoding() ?: WebUtils.DEFAULT_CHARACTER_ENCODING
    }
}
