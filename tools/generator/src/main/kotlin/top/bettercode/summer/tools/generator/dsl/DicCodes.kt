package top.bettercode.summer.tools.generator.dsl

import top.bettercode.summer.tools.lang.util.JavaType
import java.io.Serializable

/**
 * @author Peter Wu
 */
class DicCodes(
    val type: String,
    val name: String,
    var javaType: JavaType,
    val codes: LinkedHashMap<Serializable, String> = linkedMapOf()
) : Serializable