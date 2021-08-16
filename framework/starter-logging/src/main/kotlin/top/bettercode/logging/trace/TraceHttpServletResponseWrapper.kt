package top.bettercode.logging.trace

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class TraceHttpServletResponseWrapper constructor(response: HttpServletResponse) : HttpServletResponseWrapper(response) {

    private val byteArrayOutputStream = ByteArrayOutputStream()
    val cookies = ArrayList<Cookie>()

    val contentAsByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()

    override fun getOutputStream(): ServletOutputStream {
        return TraceServletOutputStream(super.getOutputStream(), byteArrayOutputStream)
    }

    override fun getWriter(): PrintWriter {
        return PrintWriter(outputStream)
    }

    override fun addCookie(cookie: Cookie) {
        super.addCookie(cookie)
        cookies.add(cookie)
    }
}
