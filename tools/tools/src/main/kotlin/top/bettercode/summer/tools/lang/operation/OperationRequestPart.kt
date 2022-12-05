package top.bettercode.summer.tools.lang.operation

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpHeaders

/**
 * A part of a multipart request.
 */
@JsonPropertyOrder("name", "submittedFileName", "headers", "contentAsString")
class OperationRequestPart(

    /**
     * Returns the name of the part.
     *
     * the name
     */
    var name: String = "",

    /**
     * Returns the name of the file that is being uploaded in this part.
     *
     * the name of the file
     */
    var submittedFileName: String? = null,

    /**
     * Returns the part's headers.
     *
     * the headers
     */
    headers: HttpHeaders = HttpHeaders(),
    /**
     * Returns the content of the response. If the response has no content an empty array
     * is returned.
     *
     * the contents, never `null`
     */
    content: ByteArray = ByteArray(0)

) : AbstractOperationMessage(headers, content)
