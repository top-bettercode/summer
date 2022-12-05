package top.bettercode.summer.tools.lang.operation

import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 * A representation of a Cookie received in a request.
 *
 * @since 2.0.7
 */
@JsonPropertyOrder("name", "value")
data class RequestCookie
/**
 * Creates a new `RequestCookie` with the given `name` and `value`.
 *
 * @param name the name of the cookie
 * @param value the value of the cookie
 */
    (
    /**
     * Returns the name of the cookie.
     *
     * the name
     */
    var name: String = "",
    /**
     * Returns the value of the cookie.
     *
     * the value
     */
    var value: String = ""
)
