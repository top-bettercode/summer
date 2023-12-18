package top.bettercode.summer.tools.lang.trace

import jakarta.servlet.http.Part
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 *
 * @author Peter Wu
 */
class TracePart(private val part: Part) : Part by part {
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private var inputStream: InputStream? = null

    val contentAsByteArray: ByteArray
        get() = byteArrayOutputStream.toByteArray()


    override fun getInputStream(): InputStream {
        if (inputStream == null) {
            inputStream = TraceInputStream(part.inputStream, byteArrayOutputStream)
        }
        return inputStream!!
    }
}