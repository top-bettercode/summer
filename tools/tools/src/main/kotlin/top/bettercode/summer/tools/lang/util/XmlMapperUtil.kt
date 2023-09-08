package top.bettercode.summer.tools.lang.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.XmlMapper

/**
 *
 * @author Peter Wu
 */
object XmlMapperUtil {

    val xmlMapper: XmlMapper by lazy {
        val mapper = XmlMapper()
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        mapper
    }

    @JvmStatic
    fun <T> toXml(obj: T): String {
        return xmlMapper.writeValueAsString(obj)
    }

    @JvmStatic
    fun <T> fromXml(xml: String, clazz: Class<T>): T {
        return xmlMapper.readValue(xml, clazz)
    }
}