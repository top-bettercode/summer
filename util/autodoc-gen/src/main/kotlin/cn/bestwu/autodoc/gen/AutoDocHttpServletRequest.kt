package cn.bestwu.autodoc.gen

import org.springframework.util.StringUtils
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import kotlin.collections.ArrayList

class AutoDocHttpServletRequest(request: HttpServletRequest?) :
    HttpServletRequestWrapper(request) {

    private val extHeaders: MutableMap<String, Array<String>> = mutableMapOf()
    private val extParams: MutableMap<String, Array<String>> = mutableMapOf()

    fun header(name: String, vararg value: String) {
        extHeaders[name] = value as Array<String>
    }

    fun param(name: String, vararg value: String) {
        extParams[name] = value as Array<String>
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
        return if (extParams.containsKey(name)) {
            StringUtils.arrayToCommaDelimitedString(extParams[name])
        } else
            super.getParameter(name)
    }

    override fun getParameterValues(name: String?): Array<String>? {
        return if (extParams.containsKey(name)) {
            extParams[name]
        } else {
            super.getParameterValues(name)
        }
    }

    override fun getParameterMap(): Map<String, Array<String>> {
        val parameterMap = mutableMapOf<String, Array<String>>()
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
        return if (extHeaders.containsKey(name)) {
            StringUtils.arrayToCommaDelimitedString(extHeaders[name])
        } else {
            super.getHeader(name)
        }
    }

    override fun getHeaders(name: String): Enumeration<String> {
        return if (extHeaders.containsKey(name)) {
            Enumerator(extHeaders[name]?.toList() ?: listOf())
        } else {
            super.getHeaders(name)
        }
    }

}