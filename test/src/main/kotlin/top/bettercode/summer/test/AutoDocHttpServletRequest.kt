package top.bettercode.summer.test

import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import top.bettercode.summer.tools.lang.operation.Parameters
import top.bettercode.summer.tools.lang.operation.QueryStringParser
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class AutoDocHttpServletRequest(request: HttpServletRequest) :
    HttpServletRequestWrapper(request) {

    private val extHeaders: MutableMap<String, Array<out String>> = mutableMapOf()
    private val extParams: MutableMap<String, Array<out String>> = mutableMapOf()

    fun isMock(): Boolean {
        return request is MockHttpServletRequest
    }

    fun isGetOrDelete(): Boolean {
        return when (method) {
            HttpMethod.GET.name, HttpMethod.DELETE.name -> true
            else -> false
        }
    }

    override fun getQueryString(): String? {
        val queryString = super.getQueryString()
        return if (isGetOrDelete()) {
            val parameterMap = mutableMapOf<String, Array<out String>>()
            parameterMap.putAll(extParams)
            parameterMap.putAll(super.getParameterMap())
            if (parameterMap.isEmpty()) {
                queryString
            } else {
                if (queryString.isNullOrBlank()) {
                    parameterMap.entries.joinToString("&") {
                        it.value.joinToString("&") { v ->
                            "${it.key}=${v}"
                        }
                    }
                } else {
                    val parameters = Parameters.parse(this)
                        .getUniqueParameters(QueryStringParser.parse(queryString))
                    if (parameters.isEmpty()) {
                        queryString
                    } else {
                        "$queryString&${
                            parameters.entries.joinToString("&") {
                                it.value.joinToString("&") { v ->
                                    "${it.key}=${v}"
                                }
                            }
                        }"
                    }
                }
            }
        } else
            queryString
    }

    fun header(name: String, vararg value: String) {
        extHeaders[name] = value
    }

    fun param(name: String, vararg value: String) {
        extParams[name] = value
    }

    override fun getParameterNames(): Enumeration<String> {
        val names: MutableList<String> = ArrayList()
        val parameterNames = super.getParameterNames()
        while (parameterNames.hasMoreElements()) {
            names.add(parameterNames.nextElement())
        }
        names.addAll(extParams.keys)
        return Enumerator(names)
    }

    override fun getParameter(name: String?): String? {
        return if (extParams.any { name.equals(it.key, true) }) {
            extParams[name]?.joinToString(",")
        } else
            super.getParameter(name)
    }

    override fun getParameterValues(name: String?): Array<out String>? {
        return if (extParams.any { name.equals(it.key, true) }) {
            extParams[name]
        } else {
            super.getParameterValues(name)
        }
    }

    override fun getParameterMap(): Map<String, Array<out String>> {
        val parameterMap = mutableMapOf<String, Array<out String>>()
        parameterMap.putAll(extParams)
        parameterMap.putAll(super.getParameterMap())
        return parameterMap
    }

    override fun getHeaderNames(): Enumeration<String> {
        val names: MutableList<String> = ArrayList()
        val headerNames = super.getHeaderNames()
        while (headerNames.hasMoreElements()) {
            names.add(headerNames.nextElement())
        }
        names.addAll(extHeaders.keys)
        return Enumerator(names)
    }

    override fun getHeader(name: String): String? {
        return if (extHeaders.any { name.equals(it.key, true) }) {
            extHeaders[name]?.joinToString(",")
        } else {
            super.getHeader(name)
        }
    }

    override fun getHeaders(name: String): Enumeration<String>? {
        return if (extHeaders.any { name.equals(it.key, true) }) {
            Enumerator(extHeaders[name]?.toList() ?: listOf())
        } else {
            super.getHeaders(name)
        }
    }

    class Enumerator(private val iterator: Iterator<String>) : Enumeration<String> {

        constructor(iterable: Iterable<String>) : this(iterable.iterator())

        override fun hasMoreElements(): Boolean {
            return iterator.hasNext()
        }

        override fun nextElement(): String {
            return iterator.next()
        }
    }
}