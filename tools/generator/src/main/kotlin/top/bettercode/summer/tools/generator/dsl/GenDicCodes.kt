package top.bettercode.summer.tools.generator.dsl

import top.bettercode.summer.tools.lang.util.JavaType
import java.io.Serializable

/**
 * @author Peter Wu
 */
class GenDicCodes(
    val type: String,
    val name: String,
    var javaType: JavaType,
    val codes: LinkedHashMap<Serializable, String> = linkedMapOf()
) : Serializable {

    companion object {

        fun convert(properties: Map<Any, Any?>): MutableMap<String, GenDicCodes> {
            val map = linkedMapOf<String, GenDicCodes>()
            val keys = properties.keys
            for (key in keys) {
                key as String
                val codeType: String
                if (key.contains(".")) {
                    codeType = key.substringBefore(".")
                    val code = key.substringAfter(".")
                    var javaType = properties["$codeType|TYPE"]?.toString() ?: "java.lang.String"
                    if (javaType == "Int") {
                        javaType = JavaType.int.fullyQualifiedNameWithoutTypeParameters
                    } else if (javaType == "String") {
                        javaType = JavaType.stringInstance.fullyQualifiedNameWithoutTypeParameters
                    }
                    val type = JavaType(javaType)
                    val dicCode = map.computeIfAbsent(codeType) {
                        GenDicCodes(
                            type = codeType,
                            name = properties[codeType]?.toString() ?: "",
                            javaType = type
                        )
                    }
                    val codeKey: Serializable =
                        if (JavaType.stringInstance == type) code else code.toInt()
                    dicCode.codes[codeKey] = properties[key]?.toString() ?: ""
                }
            }
            return map
        }

    }
}