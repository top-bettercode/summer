package top.bettercode.logging.operation

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLEncoder

/**
 * The parameters received in a request.
 *
 */
class Parameters : LinkedMultiValueMap<String, String>() {

    /**
     * Converts the parameters to a query string suitable for use in a URI or the body of
     * a form-encoded request.
     *
     * @return the query string
     */
    fun toQueryString(): String {
        val sb = StringBuilder()
        for ((key, value1) in entries) {
            if (value1.isEmpty()) {
                append(sb, key)
            } else {
                for (value in value1) {
                    append(sb, key, value)
                }
            }
        }
        return sb.toString()
    }

    /**
     * Returns a new `Parameters` containing only the parameters that do no appear
     * in the query string of the given `uri`.
     *
     * @param uri the uri
     * @return the unique parameters
     */
    fun getUniqueParameters(uri: URI): Parameters {
        val queryStringParameters = QueryStringParser.parse(uri)
        val uniqueParameters = Parameters()

        for (parameter in entries) {
            addIfUnique(parameter, queryStringParameters, uniqueParameters)
        }
        return uniqueParameters
    }

    private fun addIfUnique(parameter: Map.Entry<String, List<String>>,
                            queryStringParameters: Parameters, uniqueParameters: Parameters) {
        if (!queryStringParameters.containsKey(parameter.key)) {
            uniqueParameters[parameter.key] = parameter.value
        } else {
            val candidates = parameter.value
            val existing = queryStringParameters[parameter.key]
            for (candidate in candidates) {
                if (!existing!!.contains(candidate)) {
                    uniqueParameters.add(parameter.key, candidate)
                }
            }
        }
    }

    private fun append(sb: StringBuilder, key: String) {
        append(sb, key, "")
    }

    private fun append(sb: StringBuilder, key: String, value: String = "") {
        doAppend(sb, urlEncodeUTF8(key) + "=" + urlEncodeUTF8(value))
    }

    private fun doAppend(sb: StringBuilder, toAppend: String) {
        if (sb.isNotEmpty()) {
            sb.append("&")
        }
        sb.append(toAppend)
    }

    private fun urlEncodeUTF8(s: String): String {
        if (!StringUtils.hasLength(s)) {
            return ""
        }
        try {
            return URLEncoder.encode(s, "UTF-8")
        } catch (ex: UnsupportedEncodingException) {
            throw IllegalStateException("Unable to URL encode $s using UTF-8",
                    ex)
        }

    }

}
