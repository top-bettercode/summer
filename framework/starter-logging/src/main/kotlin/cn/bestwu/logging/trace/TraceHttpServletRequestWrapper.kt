package cn.bestwu.logging.trace

import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.TraceBufferedReader
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
    private val parts: MutableCollection<Part>? = if (request.contentType?.toLowerCase()?.startsWith("multipart/") == true)
        super.getParts().map { TracePart(it) }.toMutableList()
    else
        null

    val contentAsByteArray: ByteArray
        get() = if (isFinished()) byteArrayOutputStream.toByteArray() else try {
            StreamUtils.copyToByteArray(request.inputStream)
        } catch (e: Exception) {
            "Request inputStream has been read.Can't record the original data.".toByteArray()
        }

    private fun isFinished(): Boolean {
        return try {
            request.inputStream.isFinished
        } catch (e: AbstractMethodError) {
            byteArrayOutputStream.size() != 0
        }
    }

    override fun getPart(name: String?): Part? {
        return parts?.find { it.name == name } ?: super.getPart(name)
    }

    override fun getParts(): MutableCollection<Part> {
        return parts ?: super.getParts()
    }

    override fun getInputStream(): ServletInputStream {
        return TraceServletInputStream(super.getInputStream(), byteArrayOutputStream)
    }

    override fun getReader(): BufferedReader {
        return TraceBufferedReader(super.getReader(), byteArrayOutputStream)
    }

    override fun getCharacterEncoding(): String {
        return super.getCharacterEncoding() ?: "UTF-8"
    }
}
