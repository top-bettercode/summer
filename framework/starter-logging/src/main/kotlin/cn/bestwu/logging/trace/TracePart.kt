package cn.bestwu.logging.trace

import org.springframework.util.StreamUtils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.servlet.http.Part

/**
 *
 * @author Peter Wu
 */
class TracePart(private val part: Part) : Part by part {
    private val byteArrayOutputStream = ByteArrayOutputStream()

    val contentAsByteArray: ByteArray
        get() = if (byteArrayOutputStream.size()>0) byteArrayOutputStream.toByteArray() else try {
            StreamUtils.copyToByteArray(part.inputStream)
        } catch (e: Exception) {
            "Request part has been read.Can't record the original data.".toByteArray()
        }



    override fun getInputStream(): InputStream {
        return TraceInputStream(part.inputStream, byteArrayOutputStream)
    }
}