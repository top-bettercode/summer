package top.bettercode.summer.tools.lang.operation

import org.springframework.util.LinkedMultiValueMap
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest

/**
 * The parameters received in a request.
 *
 */
class Parameters : LinkedMultiValueMap<String, String>() {


    companion object {
        fun parse(request: HttpServletRequest): Parameters {
            val parameters = Parameters()
            for (name in request.parameterNames) {
                for (value in request.getParameterValues(name)) {
                    parameters.add(name, value)
                }
            }
            return parameters
        }
    }

    override fun get(key: String): MutableList<String>? {
        return super.get(key)
    }

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
     * @param queryStringParameters the queryStringParameters
     * @return the unique parameters
     */
    fun getUniqueParameters(queryStringParameters: Parameters): Parameters {
        val uniqueParameters = Parameters()

        for (parameter in entries) {
            addIfUnique(parameter, queryStringParameters, uniqueParameters)
        }
        return uniqueParameters
    }

    private fun addIfUnique(
        parameter: Map.Entry<String, List<String>>,
        queryStringParameters: Parameters, uniqueParameters: Parameters
    ) {
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
        if (s.isEmpty()) {
            return ""
        }
        try {
            return URLEncoder.encode(s, "UTF-8")
        } catch (ex: UnsupportedEncodingException) {
            throw IllegalStateException(
                "Unable to URL encode $s using UTF-8",
                ex
            )
        }

    }


}
