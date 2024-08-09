package top.bettercode.summer.tools.lang.operation

import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.util.*

/**
 * A parser for the query string of a URI.
 *
 */
object QueryStringParser {

    /**
     * Parses the query string of the given `uri` and returns the resulting
     * Parameters.
     *
     * @param uri the uri to parse
     * @return the parameters parsed from the query string
     */
    fun parse(uri: URI): Parameters {
        val query = uri.rawQuery
        return if (query != null) {
            parse(query)
        } else Parameters()
    }

    fun parse(query: String): Parameters {
        val parameters = Parameters()
        Scanner(query).use { scanner ->
            scanner.useDelimiter("&")
            while (scanner.hasNext()) {
                processParameter(scanner.next(), parameters)
            }
        }
        return parameters
    }

    private fun processParameter(parameter: String, parameters: Parameters) {
        val components =
            parameter.split("=".toRegex()).dropLastWhile { it.isEmpty() }
        if (components.size in 1..2) {
            val name = components[0]
            if (components.size == 2) {
                val value = components[1]
                parameters.add(decode(name), decode(value))
            } else {
                val values = parameters[name]
                if (values == null) {
                    parameters[name] = LinkedList()
                }
            }
        } else {
            throw IllegalArgumentException(
                "The parameter '$parameter' is malformed"
            )
        }
    }

    private fun decode(encoded: String): String {
        try {
            return URLDecoder.decode(encoded, "UTF-8")
        } catch (ex: UnsupportedEncodingException) {
            throw IllegalStateException(
                "Unable to URL encode $encoded using UTF-8", ex
            )
        }

    }

}
