package top.bettercode.summer.tools.generator.dsl

import top.bettercode.summer.tools.generator.dom.java.JavaType
import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes(
        val type: String,
        val name: String,
        var javaType: JavaType,
        val codes: MutableMap<Serializable, String> = mutableMapOf()
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}