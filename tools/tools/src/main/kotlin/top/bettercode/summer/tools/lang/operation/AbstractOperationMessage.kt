package top.bettercode.summer.tools.lang.operation

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

/**
 * Abstract base class for operation requests, request parts, and responses.
 *
 */
abstract class AbstractOperationMessage(
        var headers: HttpHeaders = HttpHeaders(),
        @JsonIgnore
        var content: ByteArray = ByteArray(0)
) {
    @JsonIgnore
    private val log: Logger = LoggerFactory.getLogger(AbstractOperationMessage::class.java)

    val prettyContent: ByteArray
        @JsonIgnore
        get() = PrettyPrintingContentModifier.modifyContent(content)

    val prettyContentAsString: String
        @JsonIgnore
        get() = RequestConverter.toString(try {
            this.headers.contentType
        } catch (e: Exception) {
            log.warn("解析contentType失败", e)
            null
        }?.charset, prettyContent)

    var contentAsString: String
        get() = RequestConverter.toString(this.headers.contentType?.charset, content)
        set(value) {
            content = value.toByteArray(this.headers.contentType?.charset ?: Charsets.UTF_8)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractOperationMessage) return false

        if (headers != other.headers) return false
        return content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        var result = headers.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }

}
