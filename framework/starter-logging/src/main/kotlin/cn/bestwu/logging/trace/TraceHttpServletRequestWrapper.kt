package cn.bestwu.logging.trace

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.TraceBufferedReader
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

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
    private val log: Logger = LoggerFactory.getLogger(TraceHttpServletRequestWrapper::class.java)
    private val byteArrayOutputStream = ByteArrayOutputStream()

    val contentAsByteArray: ByteArray
        get() = if (isFinished()) byteArrayOutputStream.toByteArray() else try {
            StreamUtils.copyToByteArray(request.inputStream)
        } catch (e: Exception) {
            log.error(e.message)
            byteArrayOf()
        }

    private fun isFinished(): Boolean {
        return try {
            request.inputStream.isFinished
        } catch (e: AbstractMethodError) {
            byteArrayOutputStream.size() != 0
        }
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
