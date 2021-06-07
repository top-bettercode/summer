package top.bettercode.logging.operation

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpHeaders
import java.nio.charset.Charset

/**
 * Abstract base class for operation requests, request parts, and responses.
 *
 */
abstract class AbstractOperationMessage(
        var headers: HttpHeaders = HttpHeaders(),
        @JsonIgnore
        var content: ByteArray = ByteArray(0)) {

    val prettyContent: ByteArray
        @JsonIgnore
        get() = PrettyPrintingContentModifier.modifyContent(content)

    val prettyContentAsString: String
        @JsonIgnore
        get() = toString(prettyContent)

    var contentAsString: String
        get() = toString(content)
        set(value) {
            content = value.toByteArray(extractCharsetFromContentTypeHeader() ?: Charsets.UTF_8)
        }

    private fun toString(content: ByteArray): String {
        if (content.isNotEmpty()) {
            val charset = extractCharsetFromContentTypeHeader()
            return if (charset != null)
                String(content, charset)
            else
                String(content)
        }
        return ""
    }

    private fun extractCharsetFromContentTypeHeader(): Charset? {
        if (this.headers.isEmpty()) {
            return null
        }
        val contentType = this.headers.contentType ?: return null
        return contentType.charset
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractOperationMessage) return false

        if (headers != other.headers) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headers.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }

}
