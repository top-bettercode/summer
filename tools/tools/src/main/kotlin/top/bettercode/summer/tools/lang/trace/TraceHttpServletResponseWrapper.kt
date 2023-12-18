package top.bettercode.summer.tools.lang.trace

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class TraceHttpServletResponseWrapper(response: HttpServletResponse) :
        HttpServletResponseWrapper(response) {

    private val byteArrayOutputStream = ByteArrayOutputStream()
    private var servletOutputStream: ServletOutputStream? = null
    private var servletWriter: PrintWriter? = null
    val cookies = ArrayList<Cookie>()

    val contentAsByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()

    override fun getOutputStream(): ServletOutputStream {
        if (servletOutputStream == null) {
            servletOutputStream =
                    TraceServletOutputStream(super.getOutputStream(), byteArrayOutputStream)
        }
        return servletOutputStream!!
    }

    override fun getWriter(): PrintWriter {
        if (servletWriter == null) {
            servletWriter = PrintWriter(this.outputStream)
        }
        return servletWriter!!
    }

    override fun addCookie(cookie: Cookie) {
        super.addCookie(cookie)
        cookies.add(cookie)
    }
}
