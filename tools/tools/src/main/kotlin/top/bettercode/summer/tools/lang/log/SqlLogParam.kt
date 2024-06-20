package top.bettercode.summer.tools.lang.log

import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.JavaTypeResolver

/**
 *
 * @author Peter Wu
 */
data class SqlLogParam(val index: Int, val type: String, val value: String) {
    override fun toString(): String {
        val value = when (JavaTypeResolver.type(type)?.javaType) {
            JavaType.stringInstance, JavaType.dateInstance, JavaType("java.time.LocalDate"), JavaType(
                "java.time.LocalDateTime"
            ) -> "'${this.value}'"

            else -> this.value
        }
        return "#$value"
    }
}