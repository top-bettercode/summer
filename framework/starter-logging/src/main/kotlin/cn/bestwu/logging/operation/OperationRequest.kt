package cn.bestwu.logging.operation

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import java.net.URI
import java.util.*

/**
 * The request that was sent as part of performing an operation on a RESTful service.
 */
@JsonPropertyOrder("uri", "restUri", "uriVariables", "method", "headers", "cookies", "remoteUser", "parameters", "parts", "contentAsString", "createdDate")
open class OperationRequest(

        /**
         * Returns the request's URI.
         *
         * the URI
         */
        var uri: URI = URI(""),
        /**
         * Returns the restful url path.
         *
         * the restful url path
         */
        var restUri: String = "",

        /**
         * @retrun URI variables
         */
        var uriVariables: Map<String, String> = mapOf(),
        /**
         * Returns the HTTP method of the request.
         *
         * the HTTP method
         */
        var method: HttpMethod = HttpMethod.GET,
        /**
         * Returns the headers that were included in the request.
         *
         * the headers
         */
        headers: HttpHeaders = HttpHeaders(),
        /**
         * Returns the [cookies][RequestCookie] sent with the request. If no cookies were
         * sent an empty collection is returned.
         *
         * the cookies, never `null`
         * @since 2.0.7
         */
        var cookies: Collection<RequestCookie> = listOf(),
        /**
         * 请求操作用户
         */
        var remoteUser: String = "",
        /**
         * Returns the request's parameters. For a `GET` request, the parameters are
         * derived from the query string. For a `POST` request, the parameters are
         * derived form the request's body.
         *
         * the parameters
         */
        var parameters: Parameters = Parameters(),

        /**
         * Returns the request's parts, provided that it is a multipart request. If not, then
         * an empty Collection is returned.
         *
         * the parts
         */
        var parts: Collection<OperationRequestPart> = listOf(),
        /**
         * Returns the content of the response. If the response has no content an empty array
         * is returned.
         *
         * the contents, never `null`
         */
        content: ByteArray = ByteArray(0),
        /**
         * 请求时间
         */
        var createdDate: Date = Date()
) : AbstractOperationMessage(headers, content)

