package top.bettercode.logging.trace

import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.servlet.http.Part

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