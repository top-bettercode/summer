package top.bettercode.summer.tools.lang.trace

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import javax.servlet.ServletOutputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class TraceHttpServletResponseWrapper constructor(response: HttpServletResponse) :
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
